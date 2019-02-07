package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.creditcard.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.creditcard.entities.CreditCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc.LinksResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CreditCardsResponse extends LinksResponse {
    private List<CreditCard> cards;

    public List<CreditCard> getCards() {
        return cards;
    }

    public Collection<CreditCardAccount> toCreditCardAccounts(
            Function<CreditCard, Optional<CardDetailsResponse>> creditCardDetailsSupplier,
            Consumer<String> logger) {
        return Optional.ofNullable(cards)
                .map(creditCards -> creditCards.stream()
                        .map(creditCard -> creditCardDetailsSupplier.apply(creditCard)
                                .flatMap(cardDetails -> cardDetails.calculateBalance(logger))
                                .map(creditCard::toTinkAccount)
                        )
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
                )
                .orElseGet(Collections::emptyList);
    }

    public Optional<CreditCard> find(CreditCardAccount account) {
        return Optional.ofNullable(cards)
                .map(Collection::stream)
                .flatMap(creditCards -> creditCards
                        .filter(creditCard -> creditCard.hasCreated(account))
                        .findFirst()
                );
    }
}
