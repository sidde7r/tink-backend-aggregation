package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.unittest;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.entities.account.AccountBalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;

@JsonObject
public class AccountBalanceV20TestEntity {

    private AccountBalanceEntity accountBalanceEntity;
    private double expectedBalance;
    private Double expectedAvailableCredit;

    public Amount getBalance() {
        return accountBalanceEntity.getBalance();
    }

    Optional<Amount> getAvailableCredit() {
        return accountBalanceEntity.getAvailableCredit();
    }

    double getExpectedBalance() {
        return expectedBalance;
    }

    Optional<Double> getExpectedAvailableCredit() {
        return Optional.ofNullable(expectedAvailableCredit);
    }
}
