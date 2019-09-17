package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities.DepositAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities.StockOrderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchInvestmentResponse {
    private List<DepositAccountEntity> depositAccounts;

    @JsonProperty("stockorders")
    private List<StockOrderEntity> stockOrders;

    public List<DepositAccountEntity> getDepositAccounts() {
        return depositAccounts;
    }

    public List<StockOrderEntity> getStockOrders() {
        return stockOrders;
    }
}
