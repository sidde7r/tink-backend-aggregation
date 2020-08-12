package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.configuration;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.Urls.BASE_URL;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@Setter
@JsonObject
public class DkbConfiguration implements ClientConfiguration {

    @Secret @ClientIdConfiguration private String clientId;
    @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;

    @JsonSchemaTitle("Consumer Id")
    @JsonSchemaExamples("NTlXp_uMZGvFIWY0ouB3vKDRBQgp")
    @JsonSchemaDescription("Application identifier set up in DKB developer portal")
    @Secret
    private String consumerId;

    @JsonSchemaTitle("Consumer Secret")
    @JsonSchemaExamples("ufGs5_Ff3bOpD79F_9VTjjO_Y6QL")
    @JsonSchemaDescription("Application secret set up in DKB developer portal")
    @SensitiveSecret
    private String consumerSecret;

    public String getBaseUrl() {
        return BASE_URL;
    }

    public String getClientId() {
        return getConfiguration(clientId, "Client Id");
    }

    public String getClientSecret() {
        return getConfiguration(clientSecret, "Client Secret");
    }

    public String getConsumerId() {
        return getConfiguration(consumerId, "Consumer Id");
    }

    public String getConsumerSecret() {
        return getConfiguration(consumerSecret, "Consumer Secret");
    }

    private String getConfiguration(String value, String name) {
        Preconditions.checkNotNull(
                Strings.emptyToNull(value),
                String.format(ErrorMessages.INVALID_CONFIGURATION, name));
        return value;
    }
}
