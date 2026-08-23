package fr.trollgun.optimiam.hardware.domain;

public interface ScannerDriver {
    ScanResult scanBarcode(String rawBarcode);
}
