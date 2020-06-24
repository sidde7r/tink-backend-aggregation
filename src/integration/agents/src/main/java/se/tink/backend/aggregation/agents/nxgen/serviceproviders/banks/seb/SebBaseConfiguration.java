package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.rpc.Response;

public abstract class SebBaseConfiguration {
    public abstract String getAuthBaseUrl();

    public abstract String getBaseUrl();

    public abstract String getListAccountsUrl();

    public abstract Optional<List<AccountEntity>> getAccountEntities(Response response);

    public boolean isBusinessAgent() {
        return false;
    }

    public String getOrganizationNumber() {
        return "";
    }
}
