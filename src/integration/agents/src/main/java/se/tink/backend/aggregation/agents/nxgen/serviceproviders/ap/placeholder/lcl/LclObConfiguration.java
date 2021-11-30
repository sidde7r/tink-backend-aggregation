package se.tink.backend.aggregation.agents.nxgen.serviceproviders.ap.placeholder.lcl;

import static com.google.common.base.Strings.emptyToNull;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

/**
 * This is a configuration class to make other processes able to discover the secrets configuration
 * and to be compatible. E.g.: processes like upload secrets require this kind of class to work
 * properly. This Configuration class belongs to a dummy agent.
 */
@JsonObject
public class LclObConfiguration implements ClientConfiguration {

    private static final String INVALID_CONFIGURATION_MESSAGE =
            "Invalid Configuration: %s cannot be empty or null";

    @Secret private String clientId;
    @Secret private String qsealcKeyId;

    public String getClientId() {
        Preconditions.checkNotNull(
                emptyToNull(clientId), String.format(INVALID_CONFIGURATION_MESSAGE, "Client ID"));

        return clientId;
    }

    public String getQsealcKeyId() {
        Preconditions.checkNotNull(
                emptyToNull(qsealcKeyId),
                String.format(INVALID_CONFIGURATION_MESSAGE, "QSealc key id"));

        return qsealcKeyId;
    }
}
