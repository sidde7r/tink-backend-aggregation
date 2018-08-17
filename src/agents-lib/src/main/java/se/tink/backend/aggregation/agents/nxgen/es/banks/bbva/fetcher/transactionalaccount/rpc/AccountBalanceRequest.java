package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.ContractEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountBalanceRequest {
    private List<ContractEntity> contracts;

    private AccountBalanceRequest(String id) {
        this.contracts = Collections.singletonList(new ContractEntity().setId(id));
    }

    public static AccountBalanceRequest create(String id) {
        return new AccountBalanceRequest(id);
    }
}
