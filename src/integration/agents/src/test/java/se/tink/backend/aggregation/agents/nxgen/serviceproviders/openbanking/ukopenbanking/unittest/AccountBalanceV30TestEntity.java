package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.unittest;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.entities.account.AccountBalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountBalanceV30TestEntity {

    private AccountBalanceEntity accountBalanceEntity;
    private double expectedBalance;
    private Double expectedAvailableCredit;

    public ExactCurrencyAmount getBalance() {
        return accountBalanceEntity.getBalance();
    }

    Optional<ExactCurrencyAmount> getAvailableCredit() {
        return accountBalanceEntity.getAvailableCredit();
    }

    double getExpectedBalance() {
        return expectedBalance;
    }

    Optional<Double> getExpectedAvailableCredit() {
        return Optional.ofNullable(expectedAvailableCredit);
    }
}
