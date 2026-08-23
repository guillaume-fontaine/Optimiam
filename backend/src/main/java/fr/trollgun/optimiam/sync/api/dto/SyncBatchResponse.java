package fr.trollgun.optimiam.sync.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncBatchResponse {
    private int totalOperations;
    private int syncedCount;
    private int conflictCount;
    private List<SyncOperationResult> results;

    @Builder.Default
    private Instant serverTimestamp = Instant.now();
}
