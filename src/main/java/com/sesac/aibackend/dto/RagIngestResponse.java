package com.sesac.aibackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * PDF 인제스트 결과 응답 (Day 5 심화).
 *
 * FastAPI /rag/ingest 의 IngestResponse(snake_case)를 그대로 받아 React로 전달합니다.
 * Spring 표준 camelCase 필드에 @JsonProperty 로 snake_case 키를 매핑합니다.
 */
public record RagIngestResponse(
        String filename,
        int pages,
        @JsonProperty("chunks_added") int chunksAdded,
        @JsonProperty("total_chunks") int totalChunks
) {}
