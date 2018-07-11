package se.tink.backend.aggregation.agents.nxgen.fr.banks.cic;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;

public class CicBankconfiguration implements EuroInformationConfiguration {
    @Override
    public String getUrl() {
        return CicBankConstants.URL;
    }

    @Override
    public String getTarget() {
        return CicBankConstants.TARGET;
    }
}
