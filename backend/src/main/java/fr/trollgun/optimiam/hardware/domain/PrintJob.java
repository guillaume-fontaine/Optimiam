package fr.trollgun.optimiam.hardware.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrintJob {
    private UUID jobId;
    private String productName;
    private String barcode;
    private LocalDate entryDate;
    private LocalDate expirationDate;
    private String quantityWithUnit;
    private String location;
    private String labelContent;
    @Builder.Default
    private Instant printedAt = Instant.now();
    @Builder.Default
    private boolean success = true;
}
