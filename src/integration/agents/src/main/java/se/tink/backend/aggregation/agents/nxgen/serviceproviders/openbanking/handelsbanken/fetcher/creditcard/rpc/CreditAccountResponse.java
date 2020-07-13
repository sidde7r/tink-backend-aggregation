package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.creditcard.rpc;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.creditcard.entity.CardAccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditAccountResponse {
    private List<CardAccountsEntity> cardAccounts;

    public List<CardAccountsEntity> getCardAccounts() {
        return Optional.ofNullable(cardAccounts).orElseGet(Lists::newArrayList);
    }
}
