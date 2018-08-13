package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique.BanqueTransatlantiqueConstants.FORM_DATA.APP_VERSION_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique.BanqueTransatlantiqueConstants.FORM_DATA.LOGIN_SUBPAGE_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique.BanqueTransatlantiqueConstants.FORM_DATA.TARGET_VALUE;

public class BanqueTransatlantiqueConfiguration implements EuroInformationConfiguration {
    @Override
    public String getUrl() {
        return BanqueTransatlantiqueConstants.URL;
    }

    @Override
    public String getTarget() {
        return TARGET_VALUE;
    }

    @Override
    public String getAppVersion() {
        return APP_VERSION_VALUE;
    }

    @Override
    public String getLoginSubpage() {
        return LOGIN_SUBPAGE_VALUE;
    }

    @Override
    public String getLoginInit() {
        return LOGIN_SUBPAGE_VALUE;
    }
}
