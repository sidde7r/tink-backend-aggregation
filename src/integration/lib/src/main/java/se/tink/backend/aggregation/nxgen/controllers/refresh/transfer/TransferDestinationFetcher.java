package se.tink.backend.aggregation.nxgen.controllers.refresh.transfer;

import java.util.Collection;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.agents.rpc.Account;

public interface TransferDestinationFetcher {
    TransferDestinationsResponse fetchTransferDestinationsFor(final Collection<Account> accounts);
}
