package se.tink.backend.agents.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Balance {

    private BalanceType type;
    private ExactCurrencyAmount amount;
    private List<CreditLine> creditLines;

    public BalanceType getType() {
        return type;
    }

    public void setType(BalanceType type) {
        this.type = type;
    }

    public ExactCurrencyAmount getAmount() {
        return amount;
    }

    public void setAmount(ExactCurrencyAmount amount) {
        this.amount = amount;
    }

    public List<CreditLine> getCreditLines() {
        return creditLines;
    }

    public void setCreditLines(List<CreditLine> creditLines) {
        this.creditLines = creditLines;
    }
}
