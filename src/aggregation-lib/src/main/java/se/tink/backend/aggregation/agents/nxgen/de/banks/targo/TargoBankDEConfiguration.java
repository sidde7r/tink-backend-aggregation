package se.tink.backend.aggregation.agents.nxgen.de.banks.targo;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankConfiguration;
import se.tink.backend.aggregation.nxgen.http.HeaderEnum;

public class TargoBankDEConfiguration implements TargoBankConfiguration {
    @Override
    public HeaderEnum getHost() {
        return TargoBankDEConstants.Headers.URL;
    }

    @Override
    public String getUrl() {
        return TargoBankDEConstants.URL;
    }
}
