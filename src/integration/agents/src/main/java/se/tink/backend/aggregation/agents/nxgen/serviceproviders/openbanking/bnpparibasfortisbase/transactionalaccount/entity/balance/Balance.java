package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.transactionalaccount.entity.balance;

import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.BnpParibasFortisBaseConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class Balance {

    private BalanceAmount balanceAmount;
    private String balanceType;
    private String lastCommittedTransaction;
    private String name;

    public BalanceAmount getBalanceAmount() {
        return balanceAmount;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public String getName() {
        return name;
    }

    public boolean isBalanceTypeOther() {
        return Accounts.BALANCE_TYPE_OTHER.equalsIgnoreCase(balanceType);
    }

    public ExactCurrencyAmount toTinkAmount() {
        return new ExactCurrencyAmount(
                new BigDecimal(balanceAmount.getAmount()), balanceAmount.getCurrency());
    }
}
