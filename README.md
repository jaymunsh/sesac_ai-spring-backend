# AI Backend Project

Spring Boot 3.5와 Spring Security 6, Spring Data JPA를 기반으로 구축된 AI 백엔드 교육용 학습 프로젝트입니다.

---

# 개요 및 프로젝트 스펙

### 기술 스택 (Tech Stack)
* **Language & JDK**: Java 21 (Temurin OpenJDK)
* **Framework**: Spring Boot 3.5.14
* **Security**: Spring Security 6 (Form Login + JWT + OAuth2 Client)
* **Database & ORM**: Spring Data JPA
  * **Dev Profile**: In-Memory H2 Database
  * **Prod Profile**: PostgreSQL (Docker-compose 연동 가능)
* **Libraries**:
  * **API Docs**: Springdoc OpenAPI / Swagger UI (2.8.16)
  * **JWT Utility**: JJWT (0.12.6)
  * **Lombok & Validation**

---

# 일지

### Day 1
* **학습 목표**: 파이썬 프로젝트(FastAPI) 실습을 통한 간단한 API 단위 테스트 맛보기
* **구현 내용**:
  * FastAPI 프레임워크를 기반으로 기본적인 REST API 엔드포인트를 구축하고 Swagger를 통한 단위 테스트 진행.
  * HTTP 요청 및 응답 생명주기에 대한 기본 구조 이해.

### Day 2
* **학습 목표**: 스프링 프로젝트 시작 및 REST API 설계 원칙 학습
* **구현 내용**:
  * 본 스프링 프로젝트 구축 시작 및 기본 디렉토리 레이아웃 설계.
  * `@RestController` 기반의 `/greeting` API 및 비즈니스 로직 분리를 위한 `GreetingService` 구현.
  * 데이터베이스 연동 없이 `ConcurrentHashMap`과 `AtomicLong`을 활용해 메모리 내에서 동작하는 인메모리 CRUD API 구현 (`/legacy/items/**`).
  * `@Valid` 어노테이션과 DTO 패턴을 이용한 HTTP Request Body 유효성 검증 및 예외 공통 처리를 위한 `NotFoundException` 구현.

### Day 3
* **학습 목표**: Spring Data JPA 실무 매핑 및 연관관계 설계
* **구현 내용**:
  * **수업 내용**:
    * 데이터베이스 영속성 매핑 기초를 다지기 위해 `User`와 `ChatLog` Entity 및 Repository 설계.
    * JPA 영속성 컨텍스트 학습 및 특정 유저 기준 최신 대화 로그 조회 API 개발.
    * N+1 문제를 방지하고 쿼리 최적화를 이뤄내기 위해 Fetch Join 쿼리 메소드 구현 (`findByUserIdWithUser`).
  * **과제 (별개 진행)**:
    * 사원(`Employee`) 및 부서(`Department`) 도메인 설계를 바탕으로 1:N 단방향/양방향 연관관계 실습.
    * 사원과 부서에 매핑되는 엔티티 생성, 수정, 삭제 등의 CRUD API 구현.

### Day 4
* **학습 목표**: 스프링 시큐리티 기반의 인증 인가 시스템 구축 및 소셜 로그인 통합
* **추가 내용**:
  * **Form 로그인 및 JWT 아키텍처**:
    * 사용자 비밀번호를 안전하게 보호하기 위해 **BCrypt 해시 알고리즘**을 도입하여 저장.
    * 모든 API 요청에 대해 인증 상태를 검증하는 커스텀 필터 `JwtAuthenticationFilter` 구현.
    * JWT를 해석하여 Security Context에 로드함으로써 폼 로그인 및 소셜 로그인 사용자를 통일된 인증 모델로 관리.

### Day 5
* **학습 목표**: 시스템 통합(Spring Boot, Python, React) 및 문서 기반 질의(RAG) 구현
* **구현 내용**:
  * **FastAPI 연동을 위한 WebClient 설정 (`WebClientConfig`)**:
    * Spring Boot에서 Python AI 백엔드(FastAPI)와의 리액티브 통신을 위해 `WebClient` 빈을 구성하고, 외부 API 호출 지연을 방지하기 위해 50초 연결 및 응답 타임아웃을 설정했습니다.
  * **FastAPI 연동용 클라이언트 서비스 구현**:
    * [PythonChatClient](file:///Users/sunghyuk/IdeaProjects/ai-backend/src/main/java/com/sesac/aibackend/service/PythonChatClient.java): 기본 AI 채팅 서버(`/chat`) 호출을 위한 클라이언트를 구현했습니다.
    * [PythonRagClient](file:///Users/sunghyuk/IdeaProjects/ai-backend/src/main/java/com/sesac/aibackend/service/PythonRagClient.java): PDF 문서를 멀티파트로 받아 FastAPI로 업로드하는 `/rag/ingest` 및 CrewAI 기반 RAG 질의를 처리하는 `/rag/chat` 연동 클라이언트를 개발했습니다.
  * **비즈니스 로직 및 API 컨트롤러 구축**:
    * [RagController](file:///Users/sunghyuk/IdeaProjects/ai-backend/src/main/java/com/sesac/aibackend/controller/RagController.java): PDF 파일을 받아 벡터 DB에 적재하는 API와 해당 문서 기반으로 질의응답 및 이력을 저장하는 `/rag` 엔드포인트를 구현했습니다.
    * [AdminController](file:///Users/sunghyuk/IdeaProjects/ai-backend/src/main/java/com/sesac/aibackend/controller/AdminController.java): 관리자(`ADMIN`) 권한을 가진 계정만 접근할 수 있는 회원 목록 조회, 사용자 역할(Role) 수정, 그리고 회원과 연결된 FK 제약 대화 로그를 선삭제하는 사용자 관리 API를 개발했습니다 (`@PreAuthorize("hasRole('ADMIN')")` 및 SecurityConfig 설정 적용).
  * **데이터 모델 및 인프라 개선**:
    * [ChatLogService](file:///Users/sunghyuk/IdeaProjects/ai-backend/src/main/java/com/sesac/aibackend/service/ChatLogService.java): 대화 기록 저장 시 유저 ID 대신 로그인된 username을 기준으로 조회하여 로그를 저장하도록 로직을 변경하였습니다.
    * [ChatLogController](file:///Users/sunghyuk/IdeaProjects/ai-backend/src/main/java/com/sesac/aibackend/controller/ChatLogController.java): REST API를 통한 수동 대화 생성 요청 엔드포인트를 주석 처리하여, AI 프록시를 통해서만 로그가 생성되도록 통제했습니다.
  * **통합 예외 처리 (`GlobalExceptionHandler`)**:
    * API 요청 바디 유효성 검증 오류(`@Valid`, `@Pattern` 등), 연동 에러, 존재하지 않는 리소스 조회(`NotFoundException`) 등의 상황에 대비해 중앙 집중형 예외 처리 시스템을 도입하였습니다.

---

### OAuth 2.0 구글 & 카카오 로그인 통합 및 상세 아키텍처

#### 1. OIDC(OpenID Connect)란?
* **OAuth 2.0 프로토콜을 기반으로 한 간편한 사용자 신원 확인(Identity) 레이어**입니다. OAuth 2.0은 주로 특정 자원에 접근할 권한을 부여하는 '인가(Authorization)'에 맞춰져 있는 반면, OIDC는 사용자가 누구인지 파악하는 '인증(Authentication)'을 보완하기 위해 탄생했습니다.
* OIDC를 사용하면 Access Token뿐만 아니라, 사용자 정보가 암호화 서명된 JWT 형태의 **ID Token**을 함께 발급받습니다. 이를 통해 소셜 공급자가 보증하는 불변 고유 식별키(`sub`)와 이메일/닉네임 프로필 데이터를 규격화된 표준 방식으로 즉시 받아올 수 있습니다.

#### 2. `issuer-uri` 설정은 왜 필요한가요?
구글이나 깃허브 등 세계적으로 널리 쓰이는 소셜 플랫폼은 Spring Security가 내부에 엔드포인트 기본값(인증창 주소, 토큰 교환 주소 등)을 이미 템플릿으로 저장하고 있습니다. 하지만 **카카오와 같은 국내 커스텀 공급자는 Spring Security가 기본 정보(주소)를 전혀 알지 못합니다.**

원래대로라면 카카오 연동을 위해 다음 4가지 엔드포인트를 수동으로 모두 등록해야 합니다.
1. `authorization-uri` (로그인창 주소)
2. `token-uri` (인가 코드로 토큰을 교환할 주소)
3. `user-info-uri` (유저 정보를 조회할 주소)
4. `jwk-set-uri` (서명된 ID Token을 검증할 공개키 목록 주소)

**`issuer-uri` (카카오의 경우 `https://kauth.kakao.com`)를 지정하면 이 번거로운 과정이 단번에 해결됩니다.** OIDC 표준 규격에 따라, Spring Security는 구동 시점에 `https://kauth.kakao.com/.well-known/openid-configuration` 주소를 호출하여 필요한 모든 엔드포인트 주소 목록을 자동으로 알아내어 설정합니다. 따라서 이 설정을 지우면 필요한 API 엔드포인트 주소를 찾지 못해 에러가 발생하게 됩니다.

---

#### OAuth 2.0 & OIDC 전체 로그인 및 처리 흐름

* **Step 1: 로그인 요청 시작 (Frontend → Backend)**
  * 사용자가 프런트엔드 화면에서 소셜 로그인 버튼을 클릭하면 브라우저 주소창이 백엔드가 가로채는 주소로 이동합니다. 백엔드 내부의 **`OAuth2AuthorizationRequestRedirectFilter`** 필터가 이 요청을 가로채어 다음 단계인 리다이렉트 URL을 만들 준비를 합니다.
  * **구글**: 사용자가 `http://localhost:8080/oauth2/authorization/google` 주소로 요청을 보냅니다.
  * **카카오**: 사용자가 `http://localhost:8080/oauth2/authorization/kakao` 주소로 요청을 보냅니다.

* **Step 2: 소셜 로그인 동의 페이지로 리다이렉트 (Backend → Social Server)**
  * 백엔드 서버는 사용자의 브라우저를 소셜 로그인을 할 수 있는 소셜 서버의 로그인 화면(HTTP 302 Redirect)으로 보냅니다.
  * **구글**: 백엔드가 구글의 기본 로그인창 주소(`https://accounts.google.com/o/oauth2/v2/auth`)로 이동시킵니다.
  * **카카오**: `issuer-uri`로부터 자동 로드된 카카오의 로그인창 주소(`https://kauth.kakao.com/oauth/authorize`)로 이동시킵니다.

* **Step 3: 로그인 성공 및 인가 코드 전달 (Social Server → Backend)**
  * 사용자가 소셜 화면에서 정상적으로 로그인하고 정보 제공에 동의하면, 소셜 서버는 백엔드로 '인가 코드(Code)'를 넘겨주며 지정된 리다이렉트 주소(Callback URI)로 브라우저를 다시 보냅니다.
  * **구글**: 스프링 시큐리티 내장 설정에 의해 `{baseUrl}/login/oauth2/code/google` 주소로 인가 코드가 전달됩니다.
  * **카카오**: YAML 설정에 수동 등록한 `{baseUrl}/login/oauth2/code/kakao` 주소로 인가 코드가 전달됩니다. (`{baseUrl}`은 현재 실행 중인 호스트 주소 `http://localhost:8080`으로 자동 변환됩니다.)

* **Step 4: 인가 코드로 토큰 및 유저 정보 자동 조회 (Backend ↔ Social Server)**
  * 백엔드 내부의 **`OAuth2LoginAuthenticationFilter`**가 콜백 주소로 넘어온 인가 코드를 가로채어, 구글/카카오 서버와 직접 통신(Backchannel)하여 실제 사용자 정보와 OIDC용 ID Token을 발급받아 내부적으로 `OidcUser` 객체를 생성합니다.
  * **구글**: 표준 구글 OIDC 스펙에 따라 토큰 검증 및 사용자 프로필 데이터 로드가 진행됩니다.
  * **카카오**: YAML에 설정한 `client-authentication-method: client_secret_post` 규칙에 따라 클라이언트 아이디와 비밀번호를 POST Body에 담아 카카오 서버로 전달하여 토큰을 획득합니다. `scope`에 `openid`가 명시되어 있어 ID Token 검증 및 `OidcUser` 객체 생성 과정이 구글과 동일하게 완료됩니다.

* **Step 5: 로그인 성공 핸들러 실행 및 데이터베이스 저장 분기 (Backend 내부)**
  * 소셜 인증이 최종 완료되면 [SecurityConfig.java](file:///Users/sunghyuk/IdeaProjects/ai-backend/src/main/java/com/sesac/aibackend/security/SecurityConfig.java)의 성공 핸들러 지정을 거쳐 [OAuth2LoginSuccessHandler.java](file:///Users/sunghyuk/IdeaProjects/ai-backend/src/main/java/com/sesac/aibackend/security/OAuth2LoginSuccessHandler.java)의 **`onAuthenticationSuccess`** 메서드가 자동으로 구동됩니다.
  * `OAuth2AuthenticationToken` 캐스팅을 통해 로그인 공급자 아이디(`registrationId`)를 확보하고 분기 처리를 탑니다. `OidcUser`로부터 공통 식별값인 `getSubject()`와 이메일 주소 `getEmail()`을 추출합니다.
  * **구글**: `registrationId`가 `"google"`이므로 [UserRepository.java](file:///Users/sunghyuk/IdeaProjects/ai-backend/src/main/java/com/sesac/aibackend/repository/UserRepository.java)의 `findByProviderAndProviderId` 메소드에 `OAuthProvider.GOOGLE`을 넘겨 유저를 식별하고 처음 로그인 시 `User.oauthUser` 정적 팩토리 메서드를 호출하여 데이터베이스에 저장합니다.
  * **카카오**: `registrationId`가 `"kakao"`이므로 `OAuthProvider.KAKAO`를 넘겨 유저를 식별하고 저장합니다.

* **Step 6: 백엔드 자체 JWT 토큰 발행 및 프런트엔드 복귀 (Backend → Frontend)**
  * 데이터베이스 연동이 완료되어 유저 인프라가 확인되면, 백엔드 전용 API 토큰을 발급하여 프런트엔드 화면으로 복귀시킵니다.
  * **공통**: `jwtUtil.generate(user.getUsername(), user.getRole().name())`를 통해 자체 토큰을 발행한 후, `response.sendRedirect(redirectUri + "?token=" + token)`를 호출하여 프런트엔드의 리다이렉션 콜백 주소(예: `http://localhost:5173/oauth/callback`)로 브라우저 창을 토큰 파라미터와 함께 이동시킵니다.

---

#### API 및 프로바이더 정보 출처 안내

#### Spring Security 기본 탑재 프로바이더 (CommonOAuth2Provider)
* Spring Security는 자주 사용되는 글로벌 로그인 서비스를 내장 설정으로 탑재하고 있습니다.
* **관련 클래스**: `org.springframework.security.config.oauth2.client.CommonOAuth2Provider`
* **지원 플랫폼**: Google, GitHub, Facebook, Okta (그 외 플랫폼은 커스텀 설정 필요)

#### 카카오 로그인 REST API 공식 명세
* 카카오에서 제공하는 OAuth2 및 OIDC 연동 주소 규칙 및 파라미터 상세 정보는 공식 문서를 통해 확인할 수 있습니다.
* **공식 개발자 문서**: [카카오 로그인 REST API 가이드](https://developers.kakao.com/docs/ko/kakaologin/rest-api)
* **API 매핑 정보**:
  * **인가 코드 요청**: `https://kauth.kakao.com/oauth/authorize` (백엔드 `authorization-uri`에 대응)
  * **토큰 발급 요청**: `https://kauth.kakao.com/oauth/token` (백엔드 `token-uri`에 대응)
  * **OIDC 사용자 정보 요청**: `https://kapi.kakao.com/v1/oidc/userinfo` (백엔드 `user-info-uri`에 대응)
