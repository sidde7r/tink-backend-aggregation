package se.tink.backend.aggregation.agents;

import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.core.account.TransferDestinationPattern;

public class RefreshTransferDestinationsResponse {
    private Map<Account, List<TransferDestinationPattern>> transferDestinations;

    public Map<Account, List<TransferDestinationPattern>> getTransferDestinations() {
        return transferDestinations;
    }

    public void setTransferDestinations(
            Map<Account, List<TransferDestinationPattern>> transferDestinations) {
        this.transferDestinations = transferDestinations;
    }
}
