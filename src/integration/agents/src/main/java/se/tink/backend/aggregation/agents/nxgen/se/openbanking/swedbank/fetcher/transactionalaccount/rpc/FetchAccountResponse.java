package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountResponse {

    private List<AccountEntity> accountList;

    public List<AccountEntity> getAccountList() {
        return accountList;
    }
}
