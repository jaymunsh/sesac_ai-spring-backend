package com.sesac.aibackend.service;

import com.sesac.aibackend.dto.ChatResponse;
import com.sesac.aibackend.dto.RagIngestResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * FastAPI RAG(/rag/*) 호출 클라이언트 (Day 5 심화).
 *
 * plain 채팅의 {@link PythonChatClient}와 책임을 분리한 별도 클라이언트입니다.
 * WebClient 빈(pythonWebClient)은 인프라이므로 공유하되, 호출 대상·본문 형식은
 * RAG 전용으로 둡니다.
 *
 * - chat()   : JSON {question} → FastAPI POST /rag/chat
 * - ingest() : multipart(file) → FastAPI POST /rag/ingest
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PythonRagClient {

    private final WebClient pythonWebClient;

    /** 벡터 DB를 검색하는 CrewAI 인사팀 에이전트에게 질의합니다. */
    public Mono<ChatResponse> chat(String question) {
        Map<String, Object> body = Map.of("question", question);
        return pythonWebClient.post()
                .uri("/rag/chat")
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp -> {
                    log.warn("python rag 4xx: {}", resp.statusCode());
                    return Mono.error(new IllegalArgumentException("invalid request to python rag"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, resp -> {
                    log.error("python rag 5xx: {}", resp.statusCode());
                    return Mono.error(new RuntimeException("python rag server error"));
                })
                .bodyToMono(ChatResponse.class);
    }

    /**
     * 업로드된 PDF 바이트를 FastAPI로 멀티파트 전달해 벡터 DB에 적재합니다.
     *
     * FastAPI 쪽 UploadFile 파라미터명이 "file"이므로 파트 이름을 "file"로 맞춥니다.
     * ByteArrayResource 의 파일명을 원본 파일명으로 지정해 FastAPI가 .pdf 검사를 통과하게 합니다.
     */
    public Mono<RagIngestResponse> ingest(byte[] bytes, String filename) {
        String safeName = (filename == null || filename.isBlank()) ? "uploaded.pdf" : filename;
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return safeName;
            }
        }).contentType(MediaType.APPLICATION_PDF);

        return pythonWebClient.post()
                .uri("/rag/ingest")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp -> {
                    log.warn("python ingest 4xx: {}", resp.statusCode());
                    return Mono.error(new IllegalArgumentException("invalid file for python ingest"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, resp -> {
                    log.error("python ingest 5xx: {}", resp.statusCode());
                    return Mono.error(new RuntimeException("python ingest server error"));
                })
                .bodyToMono(RagIngestResponse.class);
    }
}
