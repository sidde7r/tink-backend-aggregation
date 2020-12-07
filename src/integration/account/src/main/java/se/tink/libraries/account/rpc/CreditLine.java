package se.tink.libraries.account.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.libraries.account.enums.CreditLineType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditLine {

    private CreditLineType type;
    private boolean includedInBalance;
    private ExactCurrencyAmount amount;

    public CreditLineType getType() {
        return type;
    }

    public void setType(CreditLineType type) {
        this.type = type;
    }

    public boolean isIncludedInBalance() {
        return includedInBalance;
    }

    public void setIncludedInBalance(boolean includedInBalance) {
        this.includedInBalance = includedInBalance;
    }

    public ExactCurrencyAmount getAmount() {
        return amount;
    }

    public void setAmount(ExactCurrencyAmount amount) {
        this.amount = amount;
    }
}
