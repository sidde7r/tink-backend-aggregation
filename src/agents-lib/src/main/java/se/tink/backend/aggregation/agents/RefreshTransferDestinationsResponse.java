package se.tink.backend.aggregation.agents;

import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.core.account.TransferDestinationPattern;

public class RefreshTransferDestinationsResponse {
    private Map<Account, List<TransferDestinationPattern>> refreshedTransferDestinations;

    public Map<Account, List<TransferDestinationPattern>> getRefreshedTransferDestinations() {
        return refreshedTransferDestinations;
    }

    public void setRefreshedTransferDestinations(
            Map<Account, List<TransferDestinationPattern>> refreshedTransferDestinations) {
        this.refreshedTransferDestinations = refreshedTransferDestinations;
    }
}
