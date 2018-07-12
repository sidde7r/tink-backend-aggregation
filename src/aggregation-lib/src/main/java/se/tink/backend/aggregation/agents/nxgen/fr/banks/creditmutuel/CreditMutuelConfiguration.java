package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;

public class CreditMutuelConfiguration implements EuroInformationConfiguration {

    @Override
    public String getUrl() {
        return CreditMutuelConstants.URL;
    }

    @Override
    public String getTarget() {
        return CreditMutuelConstants.TARGET;
    }

    @Override
    public String getAppVersion() {
        return CreditMutuelConstants.APP_VERSION;
    }
}
