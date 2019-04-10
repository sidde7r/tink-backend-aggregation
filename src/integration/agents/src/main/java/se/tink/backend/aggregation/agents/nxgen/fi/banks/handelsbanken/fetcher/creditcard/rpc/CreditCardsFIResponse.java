package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.creditcard.rpc;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenFICreditCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CreditCardsFIResponse extends CreditCardsResponse<HandelsbankenFICreditCard> {

    public List<CreditCardAccount> toTinkAccounts() {
        return cards.stream()
                .map(HandelsbankenFICreditCard::toTinkAccount)
                .collect(Collectors.toList());
    }
}
