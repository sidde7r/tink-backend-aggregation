package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {

    private List<AccountEntity> accountList;

    public List<AccountEntity> getAccountList() {
        return accountList;
    }
}
