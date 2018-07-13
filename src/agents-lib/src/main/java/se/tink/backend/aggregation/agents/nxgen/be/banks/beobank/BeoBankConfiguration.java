package se.tink.backend.aggregation.agents.nxgen.be.banks.beobank;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;

public class BeoBankConfiguration implements EuroInformationConfiguration {
    @Override
    public String getUrl() {
        return BeoBankConstants.URL;
    }

    @Override
    public String getTarget() {
        return BeoBankConstants.TARGET;
    }

    @Override
    public String getAppVersion() {
        return BeoBankConstants.APP_VERSION;
    }

    @Override
    public String getLoginSubpage() {
        return BeoBankConstants.LOGIN_SUBPAGE;
    }
}
