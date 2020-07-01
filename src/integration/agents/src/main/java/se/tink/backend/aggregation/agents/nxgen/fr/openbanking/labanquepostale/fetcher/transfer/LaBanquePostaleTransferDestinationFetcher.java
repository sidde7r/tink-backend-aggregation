package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transfer;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrAispApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcher;

public class LaBanquePostaleTransferDestinationFetcher extends FrTransferDestinationFetcher {

    public LaBanquePostaleTransferDestinationFetcher(FrAispApiClient apiClient) {
        super(apiClient);
    }
}
