package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.transfer;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrAispApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcher;

public class CreditAgricoleTransferDestinationFetcher extends FrTransferDestinationFetcher {

    public CreditAgricoleTransferDestinationFetcher(FrAispApiClient apiClient) {
        super(apiClient);
    }
}
