package se.tink.backend.aggregation.agents.nxgen.se.business.seb;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.SebConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.rpc.Response;

public class SebConfiguration extends SebBaseConfiguration {
    private String orgNumber;

    public SebConfiguration(String orgNumber) {
        this.orgNumber = orgNumber;
    }

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
        return response.getBusinessAccountEntities();
    }

    @Override
    public boolean isBusinessAgent() {
        return true;
    }

    @Override
    public String getOrganizationNumber() {
        return orgNumber;
    }
}
