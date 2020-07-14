package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class SebConfiguration implements ClientConfiguration {

    @Secret
    @JsonProperty(required = true)
    @JsonSchemaDescription(
            "ClientId received from SEB's onboarding API. Usually your PSD2 authorization number followed by 'p' for Private, or 'c' for Corporate.")
    @JsonSchemaTitle("Client ID")
    @JsonSchemaExamples("PSDXX-XYZ-123456p")
    private String clientId;

    @SensitiveSecret
    @JsonProperty(required = true)
    @JsonSchemaDescription("Decrypted client secret received from SEB's onboarding API.")
    @JsonSchemaTitle("Client Secret")
    @JsonSchemaExamples("0123456789abcdefghij")
    @JsonSchemaInject(
            strings = {@JsonSchemaString(path = "pattern", value = "^[a-zA-Z0-9]{16,32}$")})
    private String clientSecret;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }
}
