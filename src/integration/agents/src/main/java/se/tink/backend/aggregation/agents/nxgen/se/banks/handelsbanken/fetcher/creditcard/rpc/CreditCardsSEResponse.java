package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.rpc;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenSECreditCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CreditCardsSEResponse extends CreditCardsResponse<HandelsbankenSECreditCard> {
//    private List<HandelsbankenSECreditCard> cards;

    public List<CreditCardAccount> toTinkAccounts(HandelsbankenSEApiClient client) {
        return cards.stream()
                .filter(HandelsbankenSECreditCard::isCreditCard)
                .map(handelsbankenCreditCard -> handelsbankenCreditCard.toTinkCreditAccount(client))
                .collect(Collectors.toList());
    }
}
