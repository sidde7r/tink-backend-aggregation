package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique;

import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique.BanqueTransatlantiqueConstants.FORM_DATA.APP_VERSION_KEY;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique.BanqueTransatlantiqueConstants.FORM_DATA.APP_VERSION_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique.BanqueTransatlantiqueConstants.FORM_DATA.TARGET_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique.BanqueTransatlantiqueConstants.URL.BASE_URL;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;

public class BanqueTransatlantiqueConfiguration implements EuroInformationConfiguration {
    @Override
    public String getUrl() {
        return BASE_URL;
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
    public String getAppVersionKey() {
        return APP_VERSION_KEY;
    }
}
