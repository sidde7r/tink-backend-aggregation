package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;

public class BanqueTransatlantiqueConfiguration implements EuroInformationConfiguration {
    @Override
    public String getUrl() {
        return BanqueTransatlantiqueConstants.URL;
    }

    @Override
    public String getTarget() {
        return BanqueTransatlantiqueConstants.TARGET;
    }

    @Override
    public String getAppVersion() {
        return BanqueTransatlantiqueConstants.APP_VERSION;
    }

    @Override
    public String getLoginSubpage() {
        return BanqueTransatlantiqueConstants.LOGIN_SUBPAGE;
    }

    @Override
    public String getLoginInit() {
        return BanqueTransatlantiqueConstants.LOGIN_SUBPAGE;
    }
}
