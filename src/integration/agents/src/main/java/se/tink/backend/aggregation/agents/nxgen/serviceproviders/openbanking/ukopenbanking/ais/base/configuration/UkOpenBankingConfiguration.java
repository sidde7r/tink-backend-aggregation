package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInt;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;

@JsonObject
public class UkOpenBankingConfiguration implements UkOpenBankingClientConfigurationAdapter {

    @JsonSchemaTitle("SSA JWT obtained from UKOB Directory")
    @JsonSchemaDescription(
            "SSA can be obtained by going to Directory, choosing appropriate software client and clicking the 'Generate' button.")
    @JsonSchemaExamples(
            "eyJhbGciOiJQUzI1NiIsImtpZCI6Ikh6YTl2NWJnREpjT25oY1VaN0JNd2JTTF80TlYwZ1NGdklqYVNYZEMtMWM9IiwidHlwIjoiSldUIn0.eyJpc3MiOiJPcGVuQmFua2luZyBMdGQiLCJpYXQiOjE1OTg4ODg2ODgsImp0aSI6ImY0NDNmYmExNDk5YzQ5NGQiLCJzb2Z0d2FyZV9lbnZpcm9ubWVudCI6InNhbmRib3giLCJzb2Z0d2FyZV9tb2RlIjoiVGVzdCIsInNvZnR3YXJlX2lkIjoibWprY2FsczFzcTI3cHYwWmxKNzhFeiIsInNvZnR3YXJlX2NsaWVudF9pZCI6Im1qa2NhbHMxc3EyN3B2MFpsSjc4RXoiLCJzb2Z0d2FyZV9jbGllbnRfbmFtZSI6IlRpbmsgU2FuZGJveCIsInNvZnR3YXJlX2NsaWVudF9kZXNjcmlwdGlvbiI6IlVzZWQgdG8gdGVzdCBzYW5kYm94Iiwic29mdHdhcmVfdmVyc2lvbiI6MS4wLCJzb2Z0d2FyZV9jbGllbnRfdXJpIjoiaHR0cHM6Ly9jb25zdW1lci50aW5rLnNlLyIsInNvZnR3YXJlX3JlZGlyZWN0X3VyaXMiOlsiaHR0cHM6Ly8xMjcuMC4wLjE6NzM1Ny9hcGkvdjEvdGhpcmRwYXJ0eS9jYWxsYmFjayJdLCJzb2Z0d2FyZV9yb2xlcyI6WyJBSVNQIiwiUElTUCJdLCJvcmdhbmlzYXRpb25fY29tcGV0ZW50X2F1dGhvcml0eV9jbGFpbXMiOnsiYXV0aG9yaXR5X2lkIjoiU0ZTQVNXRSIsInJlZ2lzdHJhdGlvbl9pZCI6IjQ0MDU5Iiwic3RhdHVzIjoiQWN0aXZlIiwiYXV0aG9yaXNhdGlvbnMiOlt7Im1lbWJlcl9zdGF0ZSI6IlNFIiwicm9sZXMiOlsiQUlTUCIsIlBJU1AiXX0seyJtZW1iZXJfc3RhdGUiOiJHQiIsInJvbGVzIjpbIkFJU1AiLCJQSVNQIl19LHsibWVtYmVyX3N0YXRlIjoiSUUiLCJyb2xlcyI6WyJBSVNQIiwiUElTUCJdfSx7Im1lbWJlcl9zdGF0ZSI6Ik5MIiwicm9sZXMiOlsiQUlTUCIsIlBJU1AiXX1dfSwic29mdHdhcmVfbG9nb191cmkiOiJodHRwczovL2Nkbi50aW5rLnNlL3RpbmstbG9nb3MvTE9XL1RpbmtfTGF4LnBuZyIsIm9yZ19zdGF0dXMiOiJBY3RpdmUiLCJvcmdfaWQiOiIwMDE1ODAwMDAxNmk0NElBQVEiLCJvcmdfbmFtZSI6IlRpbmsgQUIiLCJvcmdfY29udGFjdHMiOlt7Im5hbWUiOiJUZWNobmljYWwiLCJlbWFpbCI6Im9wZW5iYW5raW5nK3Vrb2J0ZWNoQHRpbmsuc2UiLCJwaG9uZSI6IjAwNDY3MDMyMzk2NDciLCJ0eXBlIjoiVGVjaG5pY2FsIn0seyJuYW1lIjoiQnVzaW5lc3MiLCJlbWFpbCI6Im9wZW5iYW5raW5nK3Vrb2JiaXpAdGluay5zZSIsInBob25lIjoiMDA0Njc2MDAyNDgzNyIsInR5cGUiOiJCdXNpbmVzcyJ9XSwib3JnX2p3a3NfZW5kcG9pbnQiOiJodHRwczovL2tleXN0b3JlLm9wZW5iYW5raW5ndGVzdC5vcmcudWsvMDAxNTgwMDAwMTZpNDRJQUFRLzAwMTU4MDAwMDE2aTQ0SUFBUS5qd2tzIiwib3JnX2p3a3NfcmV2b2tlZF9lbmRwb2ludCI6Imh0dHBzOi8va2V5c3RvcmUub3BlbmJhbmtpbmd0ZXN0Lm9yZy51ay8wMDE1ODAwMDAxNmk0NElBQVEvcmV2b2tlZC8wMDE1ODAwMDAxNmk0NElBQVEuandrcyIsInNvZnR3YXJlX2p3a3NfZW5kcG9pbnQiOiJodHRwczovL2tleXN0b3JlLm9wZW5iYW5raW5ndGVzdC5vcmcudWsvMDAxNTgwMDAwMTZpNDRJQUFRL21qa2NhbHMxc3EyN3B2MFpsSjc4RXouandrcyIsInNvZnR3YXJlX2p3a3NfcmV2b2tlZF9lbmRwb2ludCI6Imh0dHBzOi8va2V5c3RvcmUub3BlbmJhbmtpbmd0ZXN0Lm9yZy51ay8wMDE1ODAwMDAxNmk0NElBQVEvcmV2b2tlZC9tamtjYWxzMXNxMjdwdjBabEo3OEV6Lmp3a3MiLCJzb2Z0d2FyZV9wb2xpY3lfdXJpIjoiaHR0cHM6Ly9jb25zdW1lci50aW5rLnNlL2ludGVncml0eS1wb2xpY3kiLCJzb2Z0d2FyZV90b3NfdXJpIjoiaHR0cHM6Ly9jb25zdW1lci50aW5rLnNlL3Rlcm1zLWFuZC1jb25kaXRpb25zIiwic29mdHdhcmVfb25fYmVoYWxmX29mX29yZyI6bnVsbH0.sBFpkUCqMraUfRts2294fYxGc7d_0duTIU4s0I48B9jT8398vI8p9NsrrkexgLAnUbHIurGuGEqk3flM4pMxMVmZtCn4AFQoXDM2r2f0d7d1bbfOJ9MbgPvxi0sOEZlGZGVZogyjKtAHSdWMqDkNWLdPqLRil3PadqttDWv6mIuOFXjjYlkclSW9KgT8vI1RMgMdhLNFPJN18jNZdrZbfNtantcQcbFiOnYvpg8fB6PskOZLM8cMkRCAnBde3XLVAN8JcFz0YCO4gG23c5Kom_ftMlkKGcV3TWJX-o04g_n0k2xC1Cznr-YHQjOHX784tUv_xdoAWA9SVWnlNSl81Q")
    @JsonSchemaInject(ints = {@JsonSchemaInt(path = "minLength", value = 1000)})
    @JsonProperty(required = true)
    @Secret
    private String softwareStatementAssertion;

    @JsonSchemaTitle("Token Endpoint Auth Signing algorithm")
    @JsonSchemaDescription(
            "Algorithm which the TPP uses to authenticate with the token endpoint if using private_key_jwt or client_secret_jwt. This must be the same algorithm as the one chosen during registration. Must be specified if token_endpoint_auth_method is private_key_jwt or client_secret_jwt")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^(RS256|PS256)$")})
    @JsonSchemaExamples("PS256")
    @JsonProperty
    @Secret
    private String tokenEndpointAuthSigningAlg;

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
    @JsonProperty(required = true)
    @Secret
    private String tokenEndpointAuthMethod;

    @SensitiveSecret @ClientIdConfiguration private String clientId;

    @JsonSchemaTitle("Client secret")
    @JsonSchemaDescription("Client secret obtained during registration. Empty string if N/A.")
    @JsonSchemaExamples("dba7e5eb-4dfc-4fd1-9dc7-4bff9a30b2cf")
    @JsonProperty(required = true)
    @SensitiveSecret
    private String clientSecret;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public ClientInfo getProviderConfiguration() {
        return new ClientInfo(
                clientId, clientSecret, tokenEndpointAuthMethod, tokenEndpointAuthSigningAlg);
    }

    @Override
    public SoftwareStatementAssertion getSoftwareStatementAssertions() {
        return SoftwareStatementAssertion.fromJWT(softwareStatementAssertion);
    }
}
