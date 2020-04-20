package se.tink.backend.aggregation.agents.nxgen.se.banks.seb;

import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebBaseConfiguration;

public class SebConfiguration extends SebBaseConfiguration {
    @Override
    public String getBaseUrl() {
        return Urls.BASE;
    }

    @Override
    public String getListAccountsUrl() {
        return Urls.LIST_ACCOUNTS;
    }
}
