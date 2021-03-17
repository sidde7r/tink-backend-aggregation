package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.data;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {
    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        return accounts == null ? Collections.emptyList() : accounts;
    }
}
