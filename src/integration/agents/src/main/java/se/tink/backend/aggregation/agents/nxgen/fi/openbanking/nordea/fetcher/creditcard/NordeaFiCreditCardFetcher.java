package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.fetcher.creditcard;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.NordeaBaseCreditCardFetcher;

public class NordeaFiCreditCardFetcher extends NordeaBaseCreditCardFetcher {

    public NordeaFiCreditCardFetcher(NordeaBaseApiClient apiClient, String currency) {
        super(apiClient, currency);
    }
}
