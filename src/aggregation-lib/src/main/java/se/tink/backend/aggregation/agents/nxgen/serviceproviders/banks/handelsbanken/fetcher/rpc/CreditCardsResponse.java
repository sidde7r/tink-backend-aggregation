package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

public abstract class CreditCardsResponse<API extends HandelsbankenApiClient> extends BaseResponse {
    public abstract List<CreditCardAccount> toTinkAccounts(API client);

    public Optional<? extends HandelsbankenCreditCard> find(Account account) {
        return getCards().stream().filter(card -> card.is(account))
                .findFirst();
    }

    protected abstract List<? extends HandelsbankenCreditCard> getCards();
}
