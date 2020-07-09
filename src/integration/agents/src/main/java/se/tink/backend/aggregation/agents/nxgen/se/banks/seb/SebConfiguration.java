package se.tink.backend.aggregation.agents.nxgen.se.banks.seb;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.rpc.Response;

public class SebConfiguration extends SebBaseConfiguration {

    @Override
    public String getAuthBaseUrl() {
        return Urls.AUTH_BASE;
    }

    @Override
    public String getBaseUrl() {
        return Urls.BASE;
    }

    @Override
    public String getListAccountsUrl() {
        return Urls.LIST_ACCOUNTS;
    }

    @Override
    public Optional<List<AccountEntity>> getAccountEntities(Response response) {
        return response.getAccountEntities();
    }
}
