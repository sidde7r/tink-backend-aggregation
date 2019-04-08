package se.tink.backend.aggregation.nxgen.controllers.refresh.transfer;

import java.util.Collection;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;

public interface TransferDestinationFetcher {
    TransferDestinationsResponse fetchTransferDestinationsFor(final Collection<Account> accounts);
}
