package se.tink.backend.aggregation.agents.nxgen.se.business.seb;

import se.tink.backend.aggregation.agents.nxgen.se.business.seb.SebConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebBaseConfiguration;

public class SebConfiguration extends SebBaseConfiguration {
    @Override
    public String getBaseUrl() {
        return Urls.BASE;
    }
}
