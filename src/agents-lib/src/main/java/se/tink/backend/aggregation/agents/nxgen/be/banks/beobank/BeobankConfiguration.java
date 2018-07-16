package se.tink.backend.aggregation.agents.nxgen.be.banks.beobank;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;

public class BeobankConfiguration implements EuroInformationConfiguration {
    @Override
    public String getUrl() {
        return BeobankConstants.URL;
    }

    @Override
    public String getTarget() {
        return BeobankConstants.TARGET;
    }

    @Override
    public String getAppVersion() {
        return BeobankConstants.APP_VERSION;
    }

    @Override
    public String getLoginSubpage() {
        return BeobankConstants.LOGIN_SUBPAGE;
    }
}
