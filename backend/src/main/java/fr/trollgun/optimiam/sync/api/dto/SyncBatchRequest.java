package fr.trollgun.optimiam.sync.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncBatchRequest {
    private String clientId;

    @NotEmpty(message = "Le lot d'opérations ne peut pas être vide")
    @Valid
    private List<SyncOperationDto> operations;
}
