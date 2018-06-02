package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.HandelsbankenSECreditCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

public class CreditCardsSEResponse extends CreditCardsResponse<HandelsbankenSEApiClient> {
    private List<HandelsbankenSECreditCard> cards;

    @Override
    public List<CreditCardAccount> toTinkAccounts(HandelsbankenSEApiClient client) {
        return cards.stream()
                .filter(HandelsbankenSECreditCard::isCreditCard)
                .map(handelsbankenCreditCard -> handelsbankenCreditCard.toTinkCreditAccount(client))
                .collect(Collectors.toList());
    }

    @Override
    protected List<? extends HandelsbankenCreditCard> getCards() {
        return cards;
    }
}
