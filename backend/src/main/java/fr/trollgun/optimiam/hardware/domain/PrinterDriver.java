package fr.trollgun.optimiam.hardware.domain;

import java.util.List;

public interface PrinterDriver {
    PrintJob printLabel(PrintJob request);
    List<PrintJob> getHistory();
}
