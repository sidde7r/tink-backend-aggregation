package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class AmericanExpressV62CreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final SessionStorage sessionStorage;
    private final AmericanExpressV62Configuration configuration;

    private AmericanExpressV62CreditCardFetcher(
            SessionStorage sessionStorage, AmericanExpressV62Configuration configuration) {
        this.sessionStorage = sessionStorage;
        this.configuration = configuration;
    }

    public static AmericanExpressV62CreditCardFetcher create(
            SessionStorage sessionStorage, AmericanExpressV62Configuration config) {
        return new AmericanExpressV62CreditCardFetcher(sessionStorage, config);
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CardEntity> cardEntities =
                sessionStorage
                        .get(
                                AmericanExpressV62Constants.Tags.CARD_LIST,
                                new TypeReference<List<CardEntity>>() {})
                        .orElseGet(Collections::emptyList);

        return cardEntities.stream()
                .map(card -> card.toCreditCardAccount(configuration))
                .collect(Collectors.toList());
    }
}
