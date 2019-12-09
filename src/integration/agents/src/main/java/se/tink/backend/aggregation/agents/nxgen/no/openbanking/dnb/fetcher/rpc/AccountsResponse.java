package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity.AccountEntityResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {
    private List<AccountEntityResponse> accounts;

    public AccountsResponse() {}

    public AccountsResponse(final List<AccountEntityResponse> accounts) {
        this.accounts = accounts;
    }

    public List<AccountEntityResponse> getAccountEntities() {
        return accounts;
    }
}
