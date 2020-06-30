package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transfer;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrAispApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcher;

public class CmcicTransferDestinationFetcher extends FrTransferDestinationFetcher {

    public CmcicTransferDestinationFetcher(FrAispApiClient apiClient) {
        super(apiClient);
    }
}
