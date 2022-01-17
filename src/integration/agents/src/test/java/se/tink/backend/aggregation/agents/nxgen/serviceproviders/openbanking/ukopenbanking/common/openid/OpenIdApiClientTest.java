package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.UkOpenBankingTestFixtures.JWKS_EXAMPLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.UkOpenBankingTestFixtures.WELL_KNOWN_EXAMPLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.UkOpenBankingTestFixtures.WELL_KNOWN_URL;

import java.security.PublicKey;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.TokenRequestForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class OpenIdApiClientTest {

    private TinkHttpClient httpClient;
    private OpenIdApiClient apiClient;

    @Before
    public void setup() {
        final JwtSigner jwtSignerMock = mock(JwtSigner.class);
        final SoftwareStatementAssertion softwareStatementAssertionMock =
                mock(SoftwareStatementAssertion.class);
        final String redirectUrl = "http://redirect-url";
        final ClientInfo clientInfoMock = mock(ClientInfo.class);
        final RandomValueGenerator randomValueGeneratorMock = mock(RandomValueGenerator.class);

        httpClient = mock(TinkHttpClient.class);
        apiClient =
                new OpenIdApiClient(
                        httpClient,
                        jwtSignerMock,
                        softwareStatementAssertionMock,
                        redirectUrl,
                        clientInfoMock,
                        WELL_KNOWN_URL,
                        randomValueGeneratorMock);
    }

    @Test
    public void shouldGetJwksPublicKeys() {
        RequestBuilder wellKnownRequestBuilder = mockRequestBuilder(WELL_KNOWN_EXAMPLE);
        when(httpClient.request(eq(WELL_KNOWN_URL))).thenReturn(wellKnownRequestBuilder);
        RequestBuilder jwksRequestBuilder = mockRequestBuilder(JWKS_EXAMPLE);
        when(httpClient.request(eq(new URL("http://jwks")))).thenReturn(jwksRequestBuilder);

        Optional<Map<String, PublicKey>> jwkPublicKeys = apiClient.getJwkPublicKeys();
        assertThat(jwkPublicKeys).isPresent();
        assertThat(jwkPublicKeys.get()).hasSize(1);
        assertThat(jwkPublicKeys.get().get("external").getAlgorithm()).isEqualTo("RSA");
        assertThat(jwkPublicKeys.get().get("external").getFormat()).isEqualTo("X.509");
    }

    @Test
    public void shouldThrowAuthorizationErrorWhenPreferredTokenEndpointAuthMethodNotFound() {
        // given
        TokenRequestForm requestForm = new TokenRequestForm();
        WellKnownResponse wellKnownConfiguration =
                createWellKnownResponseWithoutTokenEndpointAuthMethod();
        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                apiClient.handleFormAuthentication(
                                        requestForm, wellKnownConfiguration));
        // then
        assertThat(throwable)
                .isExactlyInstanceOf(AuthorizationException.class)
                .hasMessage("[OpenIdApiClient]: Preferred token endpoint auth method not found.");
    }

    private RequestBuilder mockRequestBuilder(String contents) {
        RequestBuilder toReturn = mock(RequestBuilder.class);
        when(toReturn.get(eq(String.class))).thenReturn(contents);
        return toReturn;
    }

    private WellKnownResponse createWellKnownResponseWithoutTokenEndpointAuthMethod() {
        return SerializationUtils.deserializeFromString(
                "        {\"version\":\"3.0\",\"issuer\":\"https:\\/\\/oauth.tiaa.barclays.com\",\"authorization_endpoint\":\"https:\\/\\/oauth.tiaa.barclays.com\\/BarclaysCorporate\\/as\\/authorization.oauth2\",\"token_endpoint\":\"https:\\/\\/token.tiaa.barclays.com\\/as\\/token.oauth2\",\"jwks_uri\":\"https:\\/\\/keystore.openbanking.org.uk\\/0015800000jfAW1AAM\\/2SPmPNUU6KrtcgutPlkfBd.jwks\",\"scopes_supported\":[\"accounts\",\"payments\",\"fundsconfirmations\",\"openid\"],\"claims_supported\":[\"openbanking_intent_id\",\"http://openbanking.org.uk/refresh_token_expires_at\",\"sub\",\"acr\"],\"response_types_supported\":[\"code id_token\"],\"response_modes_supported\":[\"fragment\",\"query\",\"form_post\"],\"grant_types_supported\":[\"implicit\",\"authorization_code\",\"refresh_token\",\"client_credentials\"],\"subject_types_supported\":[\"public\"],\"id_token_signing_alg_values_supported\":[\"PS256\"],\"token_endpoint_auth_signing_alg_values_supported\":[\"PS256\"],\"token_endpoint_auth_methods_supported\":[\"any\"],\"claim_types_supported\":[\"normal\"],\"claims_parameter_supported\":true,\"request_parameter_supported\":true,\"request_object_signing_alg_values_supported\":[\"PS256\"],\"request_object_encryption_alg_values_supported\":[],\"request_object_encryption_enc_values_supported\":[],\"request_uri_parameter_supported\":false,\"tls_client_certificate_bound_access_tokens\":true}\n",
                WellKnownResponse.class);
    }
}
