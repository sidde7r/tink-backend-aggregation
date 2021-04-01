package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.revolut;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInt;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;

public class RevolutEEAClientConfiguration implements UkOpenBankingClientConfigurationAdapter {

    private static final String TOKEN_ENDPOINT_AUTH_METHOD = "tls_client_auth";
    private static final String TOKEN_ENDPOINT_AUTH_SIGNING_ALG = "PS256";

    @SensitiveSecret @ClientIdConfiguration private String clientId;

    @JsonSchemaTitle("Self-signed software statement")
    @JsonSchemaDescription(
            "Self-signed software statement prepared as instructed in Revolut Open Banking documentation, must match SSA from registration request. Header and signature can be re-used from example as is constant")
    @JsonSchemaExamples(
            "eyJhbGciOiJub25lIn0.eyJvcmdfbmFtZSI6Ik9yZ2FuaXphdGlvbiBOYW1lIiwic29mdHdhcmVfY2xpZW50X25hbWUiOiJTb2Z0d2FyZSBDbGllbnQgTmFtZSIsIm9yZ19qd2tzX2VuZHBvaW50IjoiaHR0cHM6Ly95b3VyLmNvbXBhbnkuY29tL2p3a3MuanNvbiIsInNvZnR3YXJlX3JlZGlyZWN0X3VyaXMiOlsiaHR0cHM6Ly95b3VyLmNvbXBhbnkuY29tL2FwaS92MS9jYWxsYmFjayJdfQ.Cg")
    @JsonSchemaInject(ints = {@JsonSchemaInt(path = "minLength", value = 200)})
    @JsonProperty(required = true)
    @Secret
    private String softwareStatementAssertion;

    @Override
    public ClientInfo getProviderConfiguration() {
        return new ClientInfo(
                clientId, TOKEN_ENDPOINT_AUTH_METHOD, TOKEN_ENDPOINT_AUTH_SIGNING_ALG);
    }

    @Override
    public SoftwareStatementAssertion getSoftwareStatementAssertions() {
        return SoftwareStatementAssertion.fromJWTJson(softwareStatementAssertion);
    }
}
