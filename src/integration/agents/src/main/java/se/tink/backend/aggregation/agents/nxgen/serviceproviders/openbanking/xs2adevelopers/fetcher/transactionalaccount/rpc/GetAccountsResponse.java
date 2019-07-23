package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetAccountsResponse {

    @JsonProperty("accounts")
    private List<AccountEntity> accountList;

    public List<AccountEntity> getAccountList() {
        return accountList;
    }

    public void setAccountList(List<AccountEntity> accountList) {
        this.accountList = accountList;
    }
}
