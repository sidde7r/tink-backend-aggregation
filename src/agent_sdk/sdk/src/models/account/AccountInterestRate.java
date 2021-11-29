package se.tink.agent.sdk.models.account;

import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class AccountInterestRate {
    private final BigDecimal value;

    AccountInterestRate(BigDecimal value) {
        this.value = value;
    }

    public static AccountInterestRate from(BigDecimal value) {
        return new AccountInterestRate(value);
    }
}
