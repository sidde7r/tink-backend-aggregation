package se.tink.backend.aggregation.agents;

import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;

public class FetchTransferDestinationsResponse {
    private final Map<Account, List<TransferDestinationPattern>> transferDestinations;

    public FetchTransferDestinationsResponse(
            Map<Account, List<TransferDestinationPattern>> transferDestinations) {
        this.transferDestinations = transferDestinations;
    }

    public Map<Account, List<TransferDestinationPattern>> getTransferDestinations() {
        return transferDestinations;
    }
}
