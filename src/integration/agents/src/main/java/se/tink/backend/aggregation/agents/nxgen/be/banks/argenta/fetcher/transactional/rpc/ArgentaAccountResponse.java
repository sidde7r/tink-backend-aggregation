package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.entity.ArgentaAccount;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ArgentaAccountResponse {
    int page;
    int nextPage;
    private List<ArgentaAccount> accounts;

    public int getPage() {
        return page;
    }

    public int getNextPage() {
        return nextPage;
    }

    public List<ArgentaAccount> getAccounts() {
        return accounts;
    }
}
