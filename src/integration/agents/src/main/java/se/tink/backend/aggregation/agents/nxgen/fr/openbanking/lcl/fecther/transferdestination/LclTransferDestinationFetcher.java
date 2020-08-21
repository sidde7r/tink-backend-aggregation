package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.transferdestination;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrAispApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcher;

public class LclTransferDestinationFetcher extends FrTransferDestinationFetcher {

    public LclTransferDestinationFetcher(FrAispApiClient apiClient) {
        super(apiClient);
    }
}
