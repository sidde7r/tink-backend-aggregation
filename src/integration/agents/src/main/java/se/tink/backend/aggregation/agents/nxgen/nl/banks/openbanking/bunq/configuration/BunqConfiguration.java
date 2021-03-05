package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class BunqConfiguration implements ClientConfiguration {
    @JsonProperty
    @JsonSchemaTitle("PSD2 Api Key")
    @JsonSchemaDescription("Api Key is a public identifier for apps.")
    @JsonSchemaExamples("2746ba10ee57a195a75c07f6c9344e0537132f885ce7f3441732c4312944af50")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^[0-9a-fA-F]{64}$")})
    @SensitiveSecret
    private String psd2ApiKey;

    @JsonProperty
    @ClientIdConfiguration
    @JsonSchemaExamples("2746ba10ee57a195a75c07f6c9344e0537132f885ce7f3441732c4312944af50")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^[0-9a-fA-F]{64}$")})
    @Secret
    private String clientId;

    @JsonProperty
    @ClientSecretsConfiguration
    @JsonSchemaExamples("2746ba10ee57a195a75c07f6c9344e0537132f885ce7f3441732c4312944af50")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^[0-9a-fA-F]{64}$")})
    @SensitiveSecret
    private String clientSecret;

    @JsonProperty
    // This is a json object serialized as a string. We don't want to present this externally in
    // console.
    private String psd2InstallationKeyPair;

    @JsonProperty
    @JsonSchemaTitle("PSD2 Installation Public Key")
    @JsonSchemaDescription("The hex encoded public key of the generated RSA installation key pair")
    @JsonSchemaExamples("2746ba10ee57a195a75c07f6c9344e0537132f885ce7f3441732c4312944af50")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^[0-9a-fA-F]+$")})
    @Secret
    private String psd2InstallationPublicKey;

    @JsonProperty
    @JsonSchemaTitle("PSD2 Installation Private Key")
    @JsonSchemaDescription("The hex encoded private key of the generated RSA installation key pair")
    @JsonSchemaExamples("2746ba10ee57a195a75c07f6c9344e0537132f885ce7f3441732c4312944af50")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^[0-9a-fA-F]+$")})
    @Secret
    private String psd2InstallationPrivateKey;

    @JsonProperty
    @JsonSchemaTitle("PSD2 Client Auth Token")
    @JsonSchemaDescription(
            "The authentication token is used to authenticate the source of the API call")
    @JsonSchemaExamples("2746ba10ee57a195a75c07f6c9344e0537132f885ce7f3441732c4312944af50")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^[0-9a-fA-F]{64}$")})
    @SensitiveSecret
    private String psd2ClientAuthToken;

    public String getPsd2InstallationKeyPair() {
        if (!Strings.isNullOrEmpty(psd2InstallationPublicKey)
                && !Strings.isNullOrEmpty(psd2InstallationPrivateKey)) {
            Map<String, String> m = new HashMap<>();
            m.put("alg", "RSA");
            m.put("pubKey", psd2InstallationPublicKey);
            m.put("privKey", psd2InstallationPrivateKey);
            return SerializationUtils.serializeToString(m);
        }

        Preconditions.checkNotNull(
                Strings.emptyToNull(psd2InstallationKeyPair),
                String.format(
                        BunqConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "PSD2 Installation Key Pair"));

        return psd2InstallationKeyPair;
    }

    @JsonIgnore
    public TokenEntity getPsd2ClientAuthToken() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psd2ClientAuthToken),
                String.format(
                        BunqConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "PSD2 Client Auth Token"));

        // psd2ClientAuthToken has been used to store a json object as string. If it is a json then
        // deserialize into TokenEntity object. Otherwise, it is storing just the token itself and
        // we can create a TokenEntity object from that alone.
        if (psd2ClientAuthToken.contains("token")) {
            return SerializationUtils.deserializeFromString(psd2ClientAuthToken, TokenEntity.class);
        }
        return new TokenEntity(psd2ClientAuthToken);
    }

    public String getPsd2ApiKey() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psd2ApiKey),
                String.format(BunqConstants.ErrorMessages.INVALID_CONFIGURATION, "PSD2 Api Key"));

        return psd2ApiKey;
    }

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(BunqConstants.ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(BunqConstants.ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }
}
