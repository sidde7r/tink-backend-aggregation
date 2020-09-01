package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transferdestinations;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.utils.SebAccountPaymentCapabilityUtil;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class SebTransferDestinationFetcher implements TransferDestinationFetcher {

    private final Storage instanceStorage;

    public SebTransferDestinationFetcher(Storage instanceStorage) {
        this.instanceStorage = instanceStorage;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        Map<Account, List<TransferDestinationPattern>> transferDestinations = new HashMap<>();
        accounts.forEach(
                account ->
                        transferDestinations.put(
                                account,
                                SebAccountPaymentCapabilityUtil
                                        .inferTransferDestinationFromAccountProductType(
                                                account, instanceStorage)));
        return new TransferDestinationsResponse(transferDestinations);
    }
}
