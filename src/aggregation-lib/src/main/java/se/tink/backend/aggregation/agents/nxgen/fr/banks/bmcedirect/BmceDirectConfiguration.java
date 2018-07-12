package se.tink.backend.aggregation.agents.nxgen.fr.banks.bmcedirect;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;

public class BmceDirectConfiguration implements EuroInformationConfiguration {
    @Override
    public String getUrl() {
        return BmceDirectConstants.URL;
    }

    @Override
    public String getTarget() {
        return BmceDirectConstants.TARGET;
    }

    @Override
    public String getAppVersion() {
        return BmceDirectConstants.APP_VERSION;
    }
}
