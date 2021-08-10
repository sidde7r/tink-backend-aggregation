package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInt;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import java.util.Base64;
import java.util.Set;
import lombok.Getter;
import se.tink.backend.aggregation.agents.consent.generators.nl.rabobank.RabobankScope;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;
import se.tink.backend.aggregation.configuration.agents.UUIDConfiguration;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
@Getter
public final class RabobankConfiguration implements ClientConfiguration {
    @JsonProperty @Secret @ClientIdConfiguration @UUIDConfiguration private String clientId;

    @JsonProperty
    @SensitiveSecret
    @ClientSecretsConfiguration
    @JsonSchemaInject(
            strings = {@JsonSchemaString(path = "pattern", value = "^[0-9a-zA-Z]{30,70}$")})
    private String clientSecret;

    @JsonProperty
    @JsonSchemaTitle("TLS key and certificate in PKCS12 format, base64 encoded")
    @JsonSchemaDescription(
            "Conversion of key and certificate to the desired format can be done with the following command: '$ openssl pkcs12 -export -inkey tls.key -in tls.crt | base64'")
    @JsonSchemaExamples("MIIMa [...] UbZfAgIIAA==")
    @JsonSchemaInject(ints = {@JsonSchemaInt(path = "minLength", value = 1000)})
    @Secret
    private String clientSSLP12;

    @JsonProperty
    @JsonSchemaTitle("Password to the TLS keystore")
    @JsonSchemaDescription("Password to the PKCS12 TLS key keystore. Empty string if N/A.")
    @JsonSchemaExamples("ThisIsVeryStrongPassword42!")
    @SensitiveSecret
    private String clientSSLKeyPassword;

    @JsonIgnore
    public byte[] getClientSSLP12bytes() {
        return Base64.getDecoder().decode(getClientSSLP12());
    }

    @JsonIgnore private RabobankUrlFactory urlFactory;

    @JsonIgnore
    public RabobankUrlFactory getUrls() {
        if (urlFactory == null) {
            urlFactory =
                    new RabobankUrlFactory(
                            new URL(RabobankConstants.AUTH_URL),
                            new URL(RabobankConstants.BASE_URL));
        }
        return urlFactory;
    }

    public static Set<RabobankScope> getRabobankScopes() {
        return Sets.newHashSet(
                RabobankScope.READ_BALANCES,
                RabobankScope.READ_TRANSACTIONS_90DAYS,
                RabobankScope.READ_TRANSACTIONS_HISTORY);
    }
}
