package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.configuration;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class FabricConfiguration implements ClientConfiguration {

    @Secret private String baseUrl;

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(FabricConstants.ErrorMessages.INVALID_CONFIGURATION, "Base URL"));

        return baseUrl;
    }
}
