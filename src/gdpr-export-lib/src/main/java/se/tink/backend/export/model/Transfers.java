package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportTransfer;

public class Transfers {

    private final List<ExportTransfer> transfers;

    public Transfers(List<ExportTransfer> transfers) {
        this.transfers = transfers;
    }

    public List<ExportTransfer> getTransfers() {
        return transfers;
    }
}
