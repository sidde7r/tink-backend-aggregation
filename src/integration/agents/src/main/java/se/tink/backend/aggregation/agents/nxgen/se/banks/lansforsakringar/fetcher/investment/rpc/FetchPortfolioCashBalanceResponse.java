package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.DepotCashBalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchPortfolioCashBalanceResponse {
    private DepotCashBalanceEntity depotCashBalance;

    public DepotCashBalanceEntity getDepotCashBalance() {
        return depotCashBalance;
    }
}
