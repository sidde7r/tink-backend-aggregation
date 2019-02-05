package se.tink.backend.aggregation.agents.nxgen.fr.banks.monabanq;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;

public class MonaBanqConfiguration implements EuroInformationConfiguration {
    @Override
    public String getUrl() {
        return MonaBanqConstants.URL;
    }

    @Override
    public String getTarget() {
        return MonaBanqConstants.TARGET;
    }

    @Override
    public String getAppVersion() {
        return MonaBanqConstants.APP_VERSION;
    }
}
