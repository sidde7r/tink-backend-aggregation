package se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountsResponse {
    private List<AccountEntity> accounts;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }

    public List<AccountEntity> getAccounts() {
        return accounts;
    }
}
