package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class CrosskeyBaseConfiguration implements ClientConfiguration {

    @JsonProperty(required = true)
    @Secret
    @JsonSchemaDescription("The base URL for API endpoint.")
    @JsonSchemaTitle("Base API URL")
    @JsonSchemaExamples("https://api.alandsbanken.se")
    private String baseAPIUrl;

    @JsonProperty(required = true)
    @Secret
    @JsonSchemaDescription("The base URL for Auth endpoint.")
    @JsonSchemaTitle("Base Auth URL")
    @JsonSchemaExamples("https://open.alandsbanken.se")
    private String baseAuthUrl;

    @JsonProperty @Secret @ClientIdConfiguration private String clientId;

    @JsonProperty(required = true)
    @Secret
    @JsonSchemaDescription("The key Id registered on cross key.")
    @JsonSchemaTitle("Key ID")
    @JsonSchemaExamples("Key1")
    private String clientSigningCertificateSerialNumber;

    @JsonProperty @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;

    public String getBaseAPIUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseAPIUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base API Url"));
        return baseAPIUrl;
    }

    public String getBaseAuthUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseAuthUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base Auth Url"));
        return baseAuthUrl;
    }

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getClientSigningCertificateSerialNumber() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningCertificateSerialNumber),
                String.format(
                        ErrorMessages.INVALID_CONFIGURATION, "Signing Certificate Serial Number"));

        return clientSigningCertificateSerialNumber;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Secret"));

        return clientSecret;
    }
}
