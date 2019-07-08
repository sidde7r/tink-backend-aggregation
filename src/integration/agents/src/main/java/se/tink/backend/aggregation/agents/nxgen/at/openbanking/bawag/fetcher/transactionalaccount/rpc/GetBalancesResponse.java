package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetBalancesResponse {

    private AccountEntity account;
    private List<BalanceBaseEntity> balances;

    public List<BalanceBaseEntity> getBalances() {
        return balances;
    }
}
