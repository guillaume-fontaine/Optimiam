package fr.trollgun.optimiam.sync.api.dto;

import fr.trollgun.optimiam.sync.domain.SyncOperationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncOperationDto {

    @NotNull(message = "L'identifiant de l'opération est obligatoire")
    private UUID operationId;

    @NotNull(message = "Le type d'opération est obligatoire")
    private SyncOperationType type;

    private UUID entityId;

    private Map<String, Object> payload;

    private Long clientVersion;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
