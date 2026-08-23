package fr.trollgun.optimiam.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final Instant timestamp;
    private final int status;
    private final ErrorCode code;
    private final String message;
    private final String path;
    private final List<ValidationError> errors;

    @Getter
    @Builder
    public static class ValidationError {
        private final String field;
        private final String message;
    }
}
