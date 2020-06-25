package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transfer;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcher;

public class SocieteGeneraleTransferDestinationFetcher extends FrTransferDestinationFetcher {

    public SocieteGeneraleTransferDestinationFetcher(
            SocieteGeneraleApiClient societeGeneraleApiClient) {
        super(societeGeneraleApiClient);
    }
}
