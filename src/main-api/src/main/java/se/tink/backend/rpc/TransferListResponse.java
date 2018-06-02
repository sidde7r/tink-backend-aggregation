package se.tink.backend.rpc;

import io.protostuff.Tag;

import java.util.List;
import se.tink.backend.core.transfer.Transfer;

public class TransferListResponse {

    @Tag(1)
    private List<Transfer> transfers;

    public List<Transfer> getTransfers() {
        return transfers;
    }

    public void setTransfers(List<Transfer> transfers) {
        this.transfers = transfers;
    }
}
