package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.AgentParsingUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CashBalanceResponse {
    private CashBalanceEntity depotCashBalance;

    public CashBalanceEntity getDepotCashBalance() {
        return depotCashBalance;
    }

    public void setDepotCashBalance(CashBalanceEntity depotCashBalance) {
        this.depotCashBalance = depotCashBalance;
    }

    @JsonIgnore
    public Double getCashValue() {
        if (depotCashBalance == null) {
            return null;
        }

        return AgentParsingUtils.parseAmount(depotCashBalance.getMoneyAvailableForPurchase());
    }
}
