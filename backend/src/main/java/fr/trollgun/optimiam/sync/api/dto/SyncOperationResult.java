package fr.trollgun.optimiam.sync.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncOperationResult {
    private UUID operationId;
    private boolean success;
    private boolean conflict;
    private String message;
    private Object resultEntity;
}
