package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.rpc;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public abstract class CreditCardsResponse<CreditCard extends HandelsbankenCreditCard>
        extends BaseResponse {

    protected List<CreditCard> cards;

    public Optional<CreditCard> find(Account account) {
        return cards.stream().filter(card -> card.is(account)).findFirst();
    }
}
