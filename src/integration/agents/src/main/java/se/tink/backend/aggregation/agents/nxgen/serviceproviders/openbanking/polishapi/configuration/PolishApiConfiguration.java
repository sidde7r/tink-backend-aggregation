package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class PolishApiConfiguration implements ClientConfiguration {

    @JsonProperty
    @SensitiveSecret
    @ClientSecretsConfiguration
    @JsonSchemaTitle("Client Secret")
    @JsonSchemaDescription(
            "Unique client secret generated during TPP registration on Bank's Developer Portal. If Bank does not expose Client Secret - upload Api Key (Client Id).")
    @JsonSchemaExamples({"ais4ohrEeYzah8aphaacae5Je4oshoe8XohQuaiyeec0eip4ri", "PSDSE-FINA-44059"})
    private String clientSecret;

    @JsonProperty
    @SensitiveSecret
    @JsonSchemaTitle("API Key (Client ID)")
    @JsonSchemaDescription(
            "Unique API key (Client ID) generated during TPP registration on Polish Bank's Developer Portal. If bank does not provide this - please put organization identifier from certificate.")
    @JsonSchemaExamples({"9799c1ab-6b27-434c-bf2e-a415a788c4e1", "PSDSE-FINA-44059"})
    private String apiKey;

    @JsonProperty
    @Secret
    @JsonSchemaTitle("PEM Endpoint")
    @JsonSchemaDescription("Uri to PEM endpoint")
    @JsonSchemaExamples("https://cdn.tink.se/eidas/tink-qsealc-2023-04-12.pem")
    private String pemEndpoint;

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(PolishApiConstants.ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientSecret;
    }

    public String getApiKey() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(apiKey),
                String.format(PolishApiConstants.ErrorMessages.INVALID_CONFIGURATION, "API key"));

        return apiKey;
    }

    public String getPemEndpoint() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(pemEndpoint),
                String.format(
                        PolishApiConstants.ErrorMessages.INVALID_CONFIGURATION, "JWKS Endpoint"));

        return pemEndpoint;
    }
}
