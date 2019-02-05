package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataDepositEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetDepositsResponse {
    private int numberOfActiveOrders;
    private List<BankdataDepositEntity> deposits;

    public int getNumberOfActiveOrders() {
        return numberOfActiveOrders;
    }

    public List<BankdataDepositEntity> getDeposits() {
        return deposits;
    }
}
