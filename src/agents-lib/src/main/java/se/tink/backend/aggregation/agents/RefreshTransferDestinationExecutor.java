package se.tink.backend.aggregation.agents;

import java.util.List;
import se.tink.backend.agents.rpc.Account;

/**
 * Interface for executing refreshes of transfer destinations.
 * Agent which supports Transfers shall implement this.
 */
public interface RefreshTransferDestinationExecutor {
    /**
     * Fetch the transfer destinations associated with accounts.
     *
     * @param accounts target accounts which want to fetch latest transfer destinations
     * @return fetch transfer destinations response
     */
    FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts);
}
