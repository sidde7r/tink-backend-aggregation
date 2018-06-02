package se.tink.backend.rpc.transfer;

import se.tink.backend.core.transfer.TransferDestination;

import java.util.Collections;
import java.util.List;

public class TransferDestinationsPerAccountResult {
    private String accountId;
    private List<TransferDestination> transferDestinations;

    public TransferDestinationsPerAccountResult(String accountId, List<TransferDestination> transferDestinations) {
        this.accountId = accountId;
        if (transferDestinations == null) {
            this.transferDestinations = Collections.emptyList();
        } else {
            this.transferDestinations = transferDestinations;
        }
    }

    public String getAccountId() {
        return accountId;
    }

    public List<TransferDestination> getTransferDestinations() {
        return transferDestinations;
    }
}
