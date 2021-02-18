package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.rpc.FetchCreditCardsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class SabadellCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private SabadellApiClient apiClient;

    public SabadellCreditCardFetcher(SabadellApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        FetchCreditCardsResponse fetchCardsResponse = apiClient.fetchCreditCards();

        /*
           response does not contain a field called "cards" so fetchCardsResponse.getCards()
           is null and without this if-block the code was throwing NPE.
        */
        if (Objects.isNull(fetchCardsResponse.getCards())) {
            return Collections.emptyList();
        }

        return fetchCardsResponse.getCards().stream()
                .filter(this::isCreditCard)
                .map(CreditCardEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    private boolean isCreditCard(CreditCardEntity cardEntity) {
        String subType = cardEntity.getCardType().getSubtype();

        return SabadellConstants.AccountTypes.CREDIT_CARD_CREDIT.equalsIgnoreCase(subType)
                || SabadellConstants.AccountTypes.CREDIT_CARD_SIN.equalsIgnoreCase(subType);
    }
}
