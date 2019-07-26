package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.entities.BalancesItemEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesResponse {

    @JsonProperty("balances")
    private List<BalancesItemEntity> balances;

    @JsonProperty("account")
    private AccountEntity account;

    public BalancesItemEntity getBalance() {
        return balances.stream()
                .sorted()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No balances found"));
    }
}
