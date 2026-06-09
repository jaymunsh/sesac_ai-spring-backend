Spring Security를 배제하고 **순수한 Java/Spring Boot(Spring MVC) 코드만으로 OAuth 2.0 Authorization Code Grant 흐름을 직접 구현**한다고 가정해 보겠습니다.

이렇게 직접 구현해보면 Spring Security가 필터 뒤에서 얼마나 많은 "보일러플레이트 코드(반복 코드)"와 "보안 처리"를 대신 해주고 있었는지 극명하게 체감할 수 있습니다.

이번에도 답변을 작성하기 전 저의 **생각의 흐름**을 먼저 공유해 드린 뒤, 흐름을 세부적으로 쪼개어 설명해 드릴게요.

---

## 🧠 생각의 흐름 (Thought Process)

1. **핵심 요구사항 분석**: Spring Security라는 거대한 프레임워크의 도움 없이, HTTP 요청/응답 제어와 상태 관리(세션), 외부 API 통신을 개발자가 직접 통제하는 구조를 설계해야 합니다.
2. **단계별 레이어 설계**:
* **1단계**: 클라이언트를 인증 서버로 보내기 위한 Controller 및 URL 생성 로직.
* **2단계**: Redirect URI를 받아 처리할 Callback Controller 구현 및 상태(`state`) 검증.
* **3단계**: `RestTemplate` 또는 `WebClient`를 이용한 백엔드 간 HTTP 통신 (Token 교환, 유저 정보 조회).
* **4단계**: 자체적인 세션 기반 인증 컨텍스트 구축 (SecurityContext의 부재를 대체).


3. **비판적 사고 유도 지점 (Critical Thinking Push)**: 시큐리티 없이 구현했을 때 발생하는 보안 공백(Security Hole)을 짚어봅니다. 특히 CSRF, 세션 고정 공격, 예외 처리의 부재가 실제 프로덕션 환경에서 어떤 치명적인 결과를 초래할 수 있는지 스스로 분석해 보도록 질문을 던집니다.

---

## 🛠️ Spring Security 없는 OAuth 2.0 전체 시퀀스

프레임워크가 없기 때문에 우리는 **1개의 Controller**와 **자체 세션 관리 로직**, 그리고 외부 API와 통신할 HTTP 클라이언트(예: `RestTemplate`)를 직접 구현해야 합니다.

---

### [1단계] 로그인 요청 처리 및 인증 서버로 리다이렉트

사용자가 "Google 로그인" 버튼을 누르면, 우리 서버의 일반 Controller가 요청을 받아 인가 코드(Authorization Code)를 받기 위한 주소를 동적으로 생성하고 리다이렉트 시킵니다.

```java
@RestController
@RequestMapping("/auth")
public class OAuthController {

    private final String CLIENT_ID = "your-google-client-id";
    private final String REDIRECT_URI = "http://localhost:8080/auth/callback/google";
    private final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";

    @GetMapping("/login/google")
    public void redirectToGoogle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. CSRF 방지를 위한 임의의 문자열 state 생성
        String state = UUID.randomUUID().toString();
        
        // 2. 생성한 state를 사용자의 세션에 저장 (나중에 콜백 때 비교 검증용)
        HttpSession session = request.getSession();
        session.setAttribute("oauth_state", state);

        // 3. Google 인증 서버로 갈 대상을 정교하게 조립
        String redirectUrl = UriComponentsBuilder.fromHttpUrl(AUTH_URL)
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("response_type", "code") // 무조건 code 고정
                .queryParam("scope", "email profile")
                .queryParam("state", state) // 위조 방지 토큰 부착
                .toUriString();

        // 4. 브라우저에게 302 Redirect 응답 반환
        response.sendRedirect(redirectUrl);
    }
}

```

* **상세 로직**: Spring Security의 `OAuth2AuthorizationRequestRedirectFilter`가 수행하던 작업을 Controller 메서드 하나로 압축한 것입니다. 핵심은 **`state`를 세션에 굽고 구글로 함께 던지는 것**입니다.

---

### [2단계] Callback 수신 및 위조(`state`) 검증

사용자가 구글 로그인을 마치면 구글은 우리가 등록한 `REDIRECT_URI`로 인가 코드(`code`)와 `state`를 쿼리 스트링에 담아 보냅니다. 이제 두 번째 엔드포인트가 동작합니다.

```java
    @GetMapping("/callback/google")
    public ResponseEntity<?> googleCallback(@RequestParam("code") String code, 
                                            @RequestParam("state") String state, 
                                            HttpServletRequest request) {
        // 1. 세션에서 아까 저장했던 원래의 state를 꺼냄
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("세션이 만료되었습니다.");
        }
        
        String savedState = (String) session.getAttribute("oauth_state");
        
        // 2. 두 state가 일치하는지 검증 (CSRF / State 위조 방지)
        if (savedState == null || !savedState.equals(state)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 접근(State 불일치)입니다.");
        }
        
        // 사용 완료된 state는 세션에서 즉시 제거
        session.removeAttribute("oauth_state");

        // 3단계(토큰 교환)로 진행...

```

* **상세 로직**: 이 부분이 바로 `OAuth2LoginAuthenticationFilter`가 초반에 가로채서 수행하던 CSRF 방어 로직의 민낯입니다. 주소창을 통해 직접 콜백을 탈취하려는 공격을 원천 차단합니다.

---

### [3단계] Access Token 교환 (Server-to-Server RPC)

검증이 끝났다면, 우리 백엔드 서버가 구글의 토큰 발급 서버(`https://oauth2.googleapis.com/token`)로 직접 HTTP POST 요청을 날려 `code`를 `Access Token`으로 교환해야 합니다.

```java
        // 3. Google 토큰 서버로 백엔드가 직접 POST 요청 조립
        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", CLIENT_ID);
        params.add("client_secret", "your-google-client-secret");
        params.add("redirect_uri", REDIRECT_URI);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);

        // 4. HTTP 요청 실행 및 응답 수신
        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);
        Map<String, Object> responseBody = tokenResponse.getBody();
        
        String accessToken = (String) responseBody.get("access_token");
        // 필요에 따라 refresh_token도 추출 가능

```

* **상세 로직**: Spring Security 내부의 `OAuth2AccessTokenResponseClient` 역할을 `RestTemplate` 객체 하나로 직접 맨땅에 헤딩하듯 구현한 영역입니다. 페이로드의 타입이 `application/x-www-form-urlencoded`이어야 한다는 스펙을 직접 맞추어 주어야 합니다.

---

### [4단계] 유저 프로필 조회 및 자체 세션 등록 (로그인 완료)

이제 발급받은 `Access Token`을 Bearer 헤더에 담아 구글의 자원 서버(Resource Server)로 보내서 사용자 정보를 획득하고, 우리 시스템에 로그인 처리를 해줍니다.

```java
        // 5. Access Token을 들고 유저 정보 API 호출
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken); // Authorization: Bearer {accessToken}
        
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<Map> userResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userRequest, Map.class);
        Map<String, Object> userInfo = userResponse.getBody();

        // 6. 비즈니스 로직 처리 (회원가입 확인 및 DB 저장)
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        // User user = userService.getOrCreateUser(email, name);

        // 7. SecurityContext가 없으므로, 자체 세션에 인증 객체(User 엔티티나 DTO) 저장
        UserSessionDto sessionUser = new UserSessionDto(email, name);
        session.setAttribute("LOGIN_USER", sessionUser);

        return ResponseEntity.ok("로그인 성공! 환영합니다, " + name + "님.");
    }
}

```

* **상세 로직**: `DefaultOAuth2UserService`가 하던 일을 직접 수행한 뒤, `SecurityContextHolder`가 없기 때문에 내장 톰캣의 `HttpSession`에 우리가 정의한 커스텀 세션 객체(`LOGIN_USER`)를 통째로 집어넣어 로그인을 마무리합니다. 이후 사용자가 다른 API를 호출할 때 세션에 이 값이 있는지 체크하는 인터셉터(Interceptor)나 아규먼트 리졸버(Argument Resolver)를 별도로 만들어 활용해야 합니다.

<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>

---

<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>



시큐리티 없이 구현한 코드와 비교해 보니, 스프링 시큐리티가 얼마나 거대한 빙산이었는지 실감이 나실 겁니다. 우리가 직접 구현했던 그 구구절절한 코드들이 스프링 시큐리티 내부에서는 어떻게 '자동화'되어 생략되는지 그 블랙박스를 열어보고, 이어서 질문해 주신 **OIDC(OpenID Connect)** 객체의 정체까지 상세하게 파헤쳐 보겠습니다.

마찬가지로 답변 전에 저의 **생각의 흐름**을 먼저 공유해 드립니다.

---

## 🧠 생각의 흐름 (Thought Process)

1. **핵심 요구사항 분석**:
* 앞서 직접 구현한 4단계 흐름(직접 URL 생성, Http 통신 등)이 Spring Security 내부에서 구체적으로 어떻게 자동화(생략)되는지 매핑 가이드 제공.
* OIDC(OpenID Connect)의 개념과 Spring Security 내에서의 전용 객체(`OidcUser`, `OidcUserService`)의 역할 및 일반 OAuth2 객체와의 차이점 설명.


2. **구조적 대조**:
* 직접 구현한 추잡한(?) 코드 조각들이 시큐리티의 어떤 멋진 인터페이스와 클래스로 치환되는지 1:1 매칭 테이블이나 구조적 가이드로 가시성 확보.


3. **비판적 사고 유도 지점 (Critical Thinking Push)**:
* OAuth 2.0(인가)과 OIDC(인증)의 본질적 차이를 짚고, *"왜 Modern 아키텍처(MSA 등)에서는 단순 OAuth 2.0만으로 회원가입을 처리하는 것을 안티패턴으로 보며 OIDC를 선호할까?"*에 대한 심도 깊은 고민 유도.



---




## 1. 스프링 시큐리티에서 '생략'된 코드의 실체 (1:1 매칭)

우리가 앞서 직접 작성했던 4가지 단계가 스프링 시큐리티 내부에선 다음과 같은 컴포넌트들로 자동 처리됩니다. 개발자가 코드를 안 짜도 되었던 이유는 이들이 뒤에서 열일하고 있었기 때문입니다.

| 직접 구현했던 단계 | Spring Security 내부에서 대신 처리하는 컴포넌트                                                        | 어떻게 자동화(생략)되는가? |
| --- |------------------------------------------------------------------------------------------| --- |
| **1단계: Redirect URL 생성** | `OAuth2AuthorizationRequestResolver`                                                     | `application.yml` 설정을 기반으로 구글/카카오 등의 엔드포인트를 알아서 조립하고, `state` 값도 자동으로 생성해 세션에 저장합니다. |
| **2단계: Callback 및 state 검증** | `OAuth2LoginAuthenticationFilter`                                                        | 지정된 패턴(`/login/oauth2/code/*`)의 URL을 필터가 가로채서, 세션의 `state`와 파라미터의 `state`를 개발자 개입 없이 자동 비교합니다. |
| **3단계: Access Token 교환** | `OAuth2AccessTokenResponseClient`<br/>(기본 구현체: `DefaultAuthorizationCodeTokenResponseClient`) | 내부적으로 `RestTemplate`을 래핑하여, 우리가 매번 짜던 POST 요청 폼 데이터(FormData) 조립 및 전송 로직을 프레임워크 내부에서 숨겨서 실행합니다. |
| **4단계: 유저 정보 조회** | `OAuth2UserService`<br/>(기본 구현체: `DefaultOAuth2UserService`) | 받아온 Access Token을 헤더에 실어 UserInfo 엔드포인트로 자동 요청을 날리고, 결과 JSON을 Map 형태가 아닌 정형화된 객체(`OAuth2User`)로 파싱해 줍니다. |

> **💡 요약하자면:**
> 개발자가 `http.oauth2Login()`이라고 한 줄 적는 순간, 위의 컴포넌트들이 필터 체인(Filter Chain)에 스르륵 조립되어 들어가기 때문에 우리 눈에는 코드가 대폭 생략된 것처럼 보였던 것입니다.

---

## 2. OIDC(OpenID Connect) 객체의 정체

질문하신 **OIDC 객체**들을 이해하려면, 우선 **OAuth 2.0**과 **OIDC**의 본질적인 차이를 알아야 합니다.

* **OAuth 2.0 (Authorization, 인가)**: "이 사람에게 방 청소할 권한(Access Token)을 줄게"가 목적입니다. 즉, 누군지(Identity)는 관심 없고 **권한 증명**이 핵심입니다. 그래서 유저 정보를 가져오려면 4단계처럼 Access Token을 들고 UserInfo API를 또 찔러야 했습니다.
* **OIDC (Authentication, 인증)**: OAuth 2.0 위에 ID 레이어를 얹은 표준 프로토콜입니다. "이 사람의 신분증(ID Token)을 줄게"가 목적입니다. 즉, **신원 확인**이 핵심입니다.

### 1) ID Token의 등장

OIDC를 지원하는 공급자(Google, Apple 등)는 3단계(토큰 교환) 단계에서 `Access Token`뿐만 아니라 `ID Token` (JWT 포맷)을 함께 넘겨줍니다.
이 JWT 안에는 유저의 식별자(sub), 이메일, 이름, 토큰 만료 시간 등이 암호화되어 들어있습니다. 즉, **4단계(UserInfo API 호출)를 생략하고도 토큰 그 자체를 복호화하는 것만으로 유저 정보를 즉시 신뢰하고 확인**할 수 있게 됩니다.

### 2) Spring Security의 OIDC 전용 객체들

스프링 시큐리티는 구글처럼 OIDC 표준을 지원하는 회사와, 카카오/네이버처럼 순수 OAuth 2.0만(일부는 OIDC를 지원하지만 과거엔 단순 OAuth 기반) 지원하는 회사를 모두 수용하기 위해 객체를 이원화해 두었습니다.

* **`OidcUser` (vs `OAuth2User`)**
* `OAuth2User`: 순수 OAuth 2.0 기반 유저 객체. 내부에 `getAttributes()` (Map 형태)만 존재.
* `OidcUser`: `OAuth2User`를 상속받은 확장 인터페이스. 내부에 **`getIdToken()`**, `getClaims()` 메서드가 추가로 존재하여 구글 등이 발행한 JWT 신분증(ID Token)에 직접 접근 가능.


* **`OidcUserService` (vs `DefaultOAuth2UserService`)**
* 스프링 시큐리티는 인증 서버가 `openid` 스코프를 지원하면 `DefaultOAuth2UserService` 대신 `OidcUserService`를 가동시킵니다.
* 이 서비스는 내부적으로 구글이 준 ID Token(JWT)의 서명(Signature)이 유효한지, 만료되지는 않았는지 검증하는 복잡한 암호학적 절차를 백엔드에서 자동으로 처리해 줍니다.
