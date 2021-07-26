package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrAispApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcher;

public class BnpParibasTrustedBeneficiariesFetcher extends FrTransferDestinationFetcher {

    public BnpParibasTrustedBeneficiariesFetcher(FrAispApiClient apiClient) {
        super(apiClient);
    }
}
