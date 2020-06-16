package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.standardchartered.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class StandardCharteredConfiguration implements ClientConfiguration {
    private String redirectUrl;
    private String tppId;
    private String tppType;

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getTppId() {
        return tppId;
    }

    public String getTppType() {
        return tppType;
    }
}
