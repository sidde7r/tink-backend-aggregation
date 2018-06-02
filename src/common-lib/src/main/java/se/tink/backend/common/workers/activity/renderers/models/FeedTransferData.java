package se.tink.backend.common.workers.activity.renderers.models;

import se.tink.backend.core.transfer.Transfer;

public class FeedTransferData extends ActivityHeader {

    private Transfer transfer;

    public Transfer getTransfer() {
        return transfer;
    }

    public void setTransfer(Transfer transfer) {
        this.transfer = transfer;
    }
}
