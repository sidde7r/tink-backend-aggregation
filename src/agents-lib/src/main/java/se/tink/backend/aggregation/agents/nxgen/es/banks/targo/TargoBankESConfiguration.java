package se.tink.backend.aggregation.agents.nxgen.es.banks.targo;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;

public class TargoBankESConfiguration implements EuroInformationConfiguration {
    @Override
    public String getUrl() {
        return TargoBankESConstants.URL;
    }

    @Override
    public String getTarget() {
        return TargoBankESConstants.TARGET;
    }

    @Override
    public String getAppVersion() {
       return TargoBankESConstants.APP_VERSION;
    }
}
