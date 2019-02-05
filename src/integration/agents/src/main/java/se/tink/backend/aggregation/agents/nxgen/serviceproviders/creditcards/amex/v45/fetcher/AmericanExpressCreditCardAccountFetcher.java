package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.authenticator.entities.CardEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AmericanExpressCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final SessionStorage sessionStorage;
    private final AmericanExpressConfiguration config;

    public AmericanExpressCreditCardAccountFetcher(
            SessionStorage sessionStorage,
            AmericanExpressConfiguration config) {
        this.sessionStorage = sessionStorage;
        this.config = config;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CardEntity> cardEntities = getCardEntities();

        return cardEntities
                .stream()
                .map(
                        cardEntity ->
                                cardEntity.toCreditCardAccount(config))
                .collect(Collectors.toList());
    }

    private List<CardEntity> getCardEntities() {

        if (!sessionStorage.containsKey(AmericanExpressConstants.Tags.CARD_LIST)) {
            throw new IllegalStateException("session storage does not contain cards");
        }
        String cardString = sessionStorage.get(AmericanExpressConstants.Tags.CARD_LIST);
        TypeReference<List<CardEntity>> listReference = new TypeReference<List<CardEntity>>() {};

        return SerializationUtils.deserializeFromString(cardString, listReference);
    }
}
