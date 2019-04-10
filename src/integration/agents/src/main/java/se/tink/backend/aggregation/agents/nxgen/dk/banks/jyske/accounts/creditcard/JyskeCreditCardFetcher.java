package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.creditcard;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.creditcard.rpc.JyskeCardsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class JyskeCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private static final AggregationLogger LOGGER =
            new AggregationLogger(JyskeCreditCardFetcher.class);

    private final JyskeApiClient apiClient;

    public JyskeCreditCardFetcher(JyskeApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        try {
            String serializedCards = this.apiClient.fetchCards();
            if (SerializationUtils.deserializeForLogging(serializedCards, JyskeCardsResponse.class)
                    .filter(JyskeCardsResponse::hasCreditCards)
                    .isPresent()) {
                LOGGER.infoExtraLong(
                        "Jyske cards: " + serializedCards, JyskeConstants.Log.CREDITCARD_LOGGING);
            }
        } catch (Exception e) {
            // Probably means the user hasn't enabled the Jyske Wallet.
            LOGGER.infoExtraLong(
                    "Jyske cards fetching failed", JyskeConstants.Log.CREDITCARD_LOGGING, e);
        }
        return Collections.emptyList();
    }
}
