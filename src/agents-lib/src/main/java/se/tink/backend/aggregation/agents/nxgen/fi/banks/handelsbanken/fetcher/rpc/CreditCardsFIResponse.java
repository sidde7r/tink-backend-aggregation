package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.rpc;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.entities.HandelsbankenFICreditCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

public class CreditCardsFIResponse extends CreditCardsResponse<HandelsbankenFIApiClient> {

    private List<HandelsbankenFICreditCard> cards;
    @Override
    public List<CreditCardAccount> toTinkAccounts(HandelsbankenFIApiClient client) {
        return cards.stream()
                .map(HandelsbankenFICreditCard::toTinkAccount).collect(Collectors.toList());
    }

    @Override
    protected List<HandelsbankenFICreditCard> getCards() {
        return cards;
    }
}
