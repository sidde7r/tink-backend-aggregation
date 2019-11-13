package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsDataEntity {
    private List<AccountEntity> accounts;

    @JsonIgnore
    public List<AccountEntity> getAccounts() {
        return accounts;
    }
}
