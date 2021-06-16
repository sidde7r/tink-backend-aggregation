package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordea.fetcher.creditcard;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.NordeaBaseCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.rpc.CreditCardTransactionResponse;

public class NordeaDkCreditCardFetcher extends NordeaBaseCreditCardFetcher {
    public NordeaDkCreditCardFetcher(NordeaBaseApiClient apiClient, String currency) {
        super(apiClient, currency, CreditCardTransactionResponse.class);
    }
}
