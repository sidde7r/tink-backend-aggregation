package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.fetcher.creditcard;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.NordeaBaseCreditCardFetcher;

public class NordeaNoCreditCardFetcher extends NordeaBaseCreditCardFetcher {
    public NordeaNoCreditCardFetcher(NordeaBaseApiClient apiClient, String currency) {
        super(apiClient, currency);
    }
}
