package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInt;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import java.util.Base64;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.TlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;

@JsonObject
public class DanskebankEUConfiguration implements UkOpenBankingClientConfigurationAdapter {

    @JsonProperty @Secret @ClientIdConfiguration private String clientId;

    @JsonProperty(required = true)
    @JsonSchemaDescription("JSON of the software statement, encoded in base64")
    @JsonSchemaTitle("Software Statement Assertion")
    @JsonSchemaExamples("eyJpc3MiOiJQU0RTR...IndlYiJ9")
    @JsonSchemaInject(ints = {@JsonSchemaInt(path = "minLength", value = 100)})
    @Secret
    private String softwareStatementAssertion;

    @JsonSchemaTitle("Token Endpoint Auth Signing algorithm")
    @JsonSchemaDescription(
            "Algorithm which the TPP uses to authenticate with the token endpoint if using private_key_jwt or client_secret_jwt. This must be the same algorithm as the one chosen during registration. Must be specified if token_endpoint_auth_method is private_key_jwt or client_secret_jwt")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^(RS256|PS256)$")})
    @JsonSchemaExamples("PS256")
    @Secret
    private String tokenEndpointAuthSigningAlg = "PS256";

    @JsonSchemaTitle("Token Endpoint Authentication Method")
    @JsonSchemaDescription(
            "Specifies which Token endpoint authentication method the TPP wants to use. This must be the same method as the one chosen during registration.")
    @JsonSchemaInject(
            strings = {
                @JsonSchemaString(
                        path = "pattern",
                        value =
                                "^(private_key_jwt|client_secret_basic|client_secret_post|tls_client_auth)$")
            })
    @JsonSchemaExamples("tls_client_auth")
    @Secret
    private String tokenEndpointAuthMethod = "tls_client_auth";

    @Override
    public ClientInfo getProviderConfiguration() {
        return new ClientInfo(clientId, "", tokenEndpointAuthMethod, tokenEndpointAuthSigningAlg);
    }

    @Override
    public SoftwareStatementAssertion getSoftwareStatementAssertions() {
        return SoftwareStatementAssertion.fromJson(
                new String(Base64.getDecoder().decode(softwareStatementAssertion)));
    }

    @Override
    public Optional<TlsConfigurationSetter> getTlsConfigurationOverride() {
        return Optional.empty();
    }
}
