package se.tink.backend.aggregation.agents.nxgen.de.banks.targo;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroInformation.EuroInformationConfiguration;

public class TargoBankDEConfiguration implements EuroInformationConfiguration {
    @Override
    public String getUrl() {
        return TargoBankDEConstants.URL;
    }

    @Override
    public String getTarget() {
        return TargoBankDEConstants.TARGET;
    }
}
