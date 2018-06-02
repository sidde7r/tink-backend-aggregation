package se.tink.backend.common.workers.activity.generators;

import java.util.List;
import se.tink.backend.core.transfer.Transfer;

public class EInvoicesActivityData {

    private List<Transfer> transfers;

    public List<Transfer> getTransfers() {
        return transfers;
    }

    public void setTransfers(List<Transfer> transfers) {
        this.transfers = transfers;
    }
}
