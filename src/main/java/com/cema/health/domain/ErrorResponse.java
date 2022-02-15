package com.cema.health.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private final List<Violation> violations = new ArrayList<>();
    private String message;
    private String details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Violation {

        private String fieldName;

        private String message;
    }
}
