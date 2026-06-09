package com.sesac.aibackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * RAG 질의 요청 본문 (Day 5 심화).
 *
 * plain 채팅의 {@link ChatRequest}(prompt)와 의미를 분리해 필드명을 question으로 둡니다.
 * FastAPI /rag/chat 의 RagRequest(question, max 500)와 정렬합니다.
 */
public record RagChatRequest(
        @NotBlank @Size(max = 500) String question
) {}
