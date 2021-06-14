package se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.Collection;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountsResponse {
    private Collection<AccountEntity> accounts;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setAccounts(Collection<AccountEntity> accounts) {
        this.accounts = accounts;
    }

    public Collection<AccountEntity> getAccounts() {
        return accounts;
    }
}
