package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {

    private BalanceAmountEntity balanceAmount;
    private String balanceType;

    BalanceAmountEntity getBalanceAmount() {
        return balanceAmount;
    }

    String getBalanceType() {
        return balanceType;
    }

    boolean isInterimAvailable() {
        return balanceType.equalsIgnoreCase(IcaBankenConstants.Account.INTERIM_AVAILABLE_BALANCE);
    }

    ExactCurrencyAmount getAmount() {
        return new ExactCurrencyAmount(balanceAmount.getAmount(), balanceAmount.getCurrency());
    }
}
