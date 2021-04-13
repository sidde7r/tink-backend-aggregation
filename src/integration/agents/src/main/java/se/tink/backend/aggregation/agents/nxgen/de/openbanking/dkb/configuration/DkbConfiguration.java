package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.configuration;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.Urls.BASE_URL;

import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@Setter
@JsonObject
public class DkbConfiguration implements ClientConfiguration {

    public String getBaseUrl() {
        return BASE_URL;
    }
}
