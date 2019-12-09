package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.balance;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

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

    public Amount toTinkAmount() {
        return new Amount(
                balanceAmount.getCurrency(), Double.parseDouble(balanceAmount.getAmount()));
    }
}
