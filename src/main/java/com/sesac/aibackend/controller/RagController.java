package com.sesac.aibackend.controller;

import com.sesac.aibackend.dto.ChatResponse;
import com.sesac.aibackend.dto.RagChatRequest;
import com.sesac.aibackend.dto.RagIngestResponse;
import com.sesac.aibackend.service.ChatLogService;
import com.sesac.aibackend.service.PythonRagClient;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * /rag 프록시 컨트롤러 (Day 5 심화) — 문서 기반 질의(RAG) 전용.
 *
 * plain 채팅의 {@link ChatController}와 책임을 분리한 별도 엔드포인트입니다.
 * 흐름:
 * - POST /rag/ingest : PDF 업로드 → FastAPI 적재(벡터 DB)
 * - POST /rag/chat   : 인증 사용자 질의 → FastAPI 검색·답변 → ChatLog 저장 → 응답
 *
 * 인가: SecurityConfig 의 anyRequest().authenticated() 로 /rag/** 는 JWT 필요.
 * 로깅: 채팅 이력 저장은 plain 채팅과 같은 ChatLogService 인프라를 재사용합니다.
 *
 * block() 동기 호출은 Spring MVC(서블릿) 워커 스레드에서 안전합니다(ChatController 주석 참조).
 */
@Slf4j
@RestController
@RequestMapping("/rag")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RagController {

    private final PythonRagClient ragClient;
    private final ChatLogService chatLogService;

    @PostMapping("/chat")
    public ChatResponse chat(
            @Valid @RequestBody RagChatRequest req,
            @AuthenticationPrincipal UserDetails user) {

        ChatResponse response = ragClient.chat(req.question()).block();
        if (response == null) {
            throw new IllegalStateException("python rag returned null");
        }

        chatLogService.save(user.getUsername(), req.question(), response.answer());
        return response;
    }

    @PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RagIngestResponse ingest(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails user) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일입니다.");
        }
        log.debug("rag ingest by {}: {} ({} bytes)", user.getUsername(), file.getOriginalFilename(), file.getSize());

        RagIngestResponse response = ragClient.ingest(file.getBytes(), file.getOriginalFilename()).block();
        if (response == null) {
            throw new IllegalStateException("python ingest returned null");
        }
        return response;
    }
}
