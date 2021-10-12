package se.tink.backend.aggregation.agents.nxgen.ie.openbanking.aib;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;

public class AibClientConfiguration implements UkOpenBankingClientConfigurationAdapter {
    private static final String TOKEN_ENDPOINT_AUTH_METHOD = "client_secret_basic";
    private static final String TOKEN_ENDPOINT_AUTH_SIGNING_ALG = "PS256";

    @SensitiveSecret @ClientIdConfiguration private String clientId;

    @JsonSchemaTitle("Client secret")
    @JsonSchemaDescription("consumerSecret obtained during the registration.")
    @JsonSchemaExamples("X0Wj9bgrK5EfzTTxpBGsHQ")
    @JsonProperty(required = true)
    @SensitiveSecret
    private String clientSecret;

    @JsonSchemaExamples("https://cdn.tink.se/eidas/active-jwks.json")
    @JsonProperty(required = true)
    @Secret
    private String jwksEndpoint;

    @Override
    public ClientInfo getProviderConfiguration() {
        return new ClientInfo(
                clientId,
                clientSecret,
                TOKEN_ENDPOINT_AUTH_METHOD,
                TOKEN_ENDPOINT_AUTH_SIGNING_ALG);
    }

    @Override
    public SoftwareStatementAssertion getSoftwareStatementAssertions() {
        return SoftwareStatementAssertion.fromJwksEndpointOnly(jwksEndpoint);
    }
}
