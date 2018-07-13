package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse extends OmaspBaseResponse {
    private List<AccountsEntity> accounts;

    public List<AccountsEntity> getAccounts() {
        return accounts;
    }
}
