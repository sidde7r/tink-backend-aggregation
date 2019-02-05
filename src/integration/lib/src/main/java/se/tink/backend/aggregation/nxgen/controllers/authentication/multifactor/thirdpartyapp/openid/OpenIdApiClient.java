package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.ClientRegistration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.JsonWebKeySet;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.TokenRequestForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class OpenIdApiClient {

    protected final TinkHttpClient httpClient;
    protected final SoftwareStatement softwareStatement;
    protected final ProviderConfiguration providerConfiguration;
    private final OpenIdConstants.ClientMode clientMode;

    // Internal caching. Do not use these fields directly, always use the getters!
    private WellKnownResponse cachedWellKnownResponse;
    private JsonWebKeySet cachedProviderKeys;
    private OpenIdAuthenticatedHttpFilter authFilter;

    public OpenIdApiClient(TinkHttpClient httpClient, SoftwareStatement softwareStatement,
            ProviderConfiguration providerConfiguration, OpenIdConstants.ClientMode clientMode) {
        this.httpClient = httpClient;
        this.softwareStatement = softwareStatement;
        this.providerConfiguration = providerConfiguration;
        this.clientMode = clientMode;

        // Softw. Transp. key
        httpClient.setSslClientCertificate(
                softwareStatement.getTransportKeyP12(),
                softwareStatement.getTransportKeyPassword());
    }

    public WellKnownResponse getWellKnownConfiguration() {
        if (Objects.nonNull(cachedWellKnownResponse)) {
            return cachedWellKnownResponse;
        }

        cachedWellKnownResponse = httpClient.request(providerConfiguration.getWellKnownURL())
                .get(WellKnownResponse.class);

        return cachedWellKnownResponse;
    }

    public SoftwareStatement getSoftwareStatement() {
        return softwareStatement;
    }

    public ProviderConfiguration getProviderConfiguration() {
        return providerConfiguration;
    }

    public JsonWebKeySet getProviderKeys() {
        if (Objects.nonNull(cachedProviderKeys)) {
            return cachedProviderKeys;
        }

        WellKnownResponse providerConfiguration = getWellKnownConfiguration();
        cachedProviderKeys = httpClient.request(providerConfiguration.getJwksUri())
                .get(JsonWebKeySet.class);

        return cachedProviderKeys;
    }

    private TokenRequestForm createTokenRequestForm(String grantType) {
        WellKnownResponse wellknownConfiguration = getWellKnownConfiguration();

        // Token request does not use OpenId scope
        String scope = wellknownConfiguration
                .verifyAndGetScopes(Collections.singletonList(clientMode.getValue()))
                .orElseThrow(() -> new IllegalStateException(
                        "Provider does not support the mandatory scopes.")
                );

        TokenRequestForm requestForm = new TokenRequestForm()
                .withGrantType(grantType)
                .withScope(scope)
                .withRedirectUri(softwareStatement.getRedirectUri());

        OpenIdConstants.TOKEN_ENDPOINT_AUTH_METHOD authMethod = wellknownConfiguration
                .getPreferredTokenEndpointAuthMethod(OpenIdConstants.PREFERRED_TOKEN_ENDPOINT_AUTH_METHODS)
                .orElseThrow(() -> new IllegalStateException("Preferred token endpoint auth method not found."));

        ClientInfo clientInfo = providerConfiguration.getClientInfo();
        switch (authMethod) {
        case client_secret_post:
            requestForm.withClientSecretPost(clientInfo.getClientId(), clientInfo.getClientSecret());
            break;

        case private_key_jwt:
            requestForm.withPrivateKeyJwt(softwareStatement, wellknownConfiguration, clientInfo);
            break;

        case client_secret_basic:
            // Add to header.
            break;

        case tls_client_auth:
            // Do nothing. We authenticate using client certificate.
            break;

        default:
            throw new IllegalStateException(String.format("Not yet implemented auth method: %s",
                    authMethod.toString()));
        }

        return requestForm;
    }

    private RequestBuilder createTokenRequest() {
        WellKnownResponse wellknownConfiguration = getWellKnownConfiguration();

        RequestBuilder requestBuilder = httpClient.request(wellknownConfiguration.getTokenEndpoint())
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);

        ClientInfo clientInfo = providerConfiguration.getClientInfo();

        OpenIdConstants.TOKEN_ENDPOINT_AUTH_METHOD authMethod = wellknownConfiguration
                .getPreferredTokenEndpointAuthMethod(OpenIdConstants.PREFERRED_TOKEN_ENDPOINT_AUTH_METHODS)
                .orElseThrow(() -> new IllegalStateException("Preferred token endpoint auth method not found."));

        switch (authMethod) {
        case client_secret_basic:
            // `client_secret_basic` does not add data to the body, but on the header.
            requestBuilder = requestBuilder.addBasicAuth(clientInfo.getClientId(), clientInfo.getClientSecret());
            break;
        case tls_client_auth:
            // `tls_client_auth` needs clientId in the header.
            requestBuilder = requestBuilder.addBasicAuth(clientInfo.getClientId());
            break;

        case private_key_jwt:
            // Add to header.
            break;

        case client_secret_post:
            // Do nothing. We authenticate using client certificate.
            break;

        default:
            throw new IllegalStateException(String.format("Not yet implemented auth method: %s",
                    authMethod.toString()));
        }

        return requestBuilder;
    }

    public OAuth2Token requestClientCredentials() {
        TokenRequestForm postData = createTokenRequestForm("client_credentials");

        return createTokenRequest()
                .body(postData)
                .post(TokenResponse.class)
                .toAccessToken();
    }

    public OAuth2Token refreshAccessToken(String refreshToken) {
        TokenRequestForm postData = createTokenRequestForm("refresh_token")
                .withRefreshToken(refreshToken);

        return createTokenRequest()
                .body(postData)
                .post(TokenResponse.class)
                .toAccessToken();
    }

    public OAuth2Token exchangeAccessCode(String code) {
        TokenRequestForm postData = createTokenRequestForm("authorization_code")
                .withCode(code);

        return createTokenRequest()
                .body(postData)
                .post(TokenResponse.class)
                .toAccessToken();
    }

    public URL buildAuthorizeUrl(String state, String nonce) {
        WellKnownResponse wellknownConfiguration = getWellKnownConfiguration();
        ClientInfo clientInfo = providerConfiguration.getClientInfo();

        String responseType = OpenIdConstants.MANDATORY_RESPONSE_TYPES.stream()
                .collect(Collectors.joining(" "));

        String scope = wellknownConfiguration.verifyAndGetScopes(
                Arrays.asList(OpenIdConstants.Scopes.OPEN_ID, clientMode.getValue()))
                .orElseThrow(() -> new IllegalStateException(
                        "Provider does not support the mandatory scopes.")
                );

        /*  'response_type=id_token' only supports 'response_mode=fragment',
         *  setting 'response_mode=query' has no effect the the moment.
         */
        return wellknownConfiguration.getAuthorizationEndpoint()
                .queryParam(OpenIdConstants.Params.RESPONSE_TYPE, responseType)
                .queryParam(OpenIdConstants.Params.CLIENT_ID, clientInfo.getClientId())
                .queryParam(OpenIdConstants.Params.SCOPE, scope)
                .queryParam(OpenIdConstants.Params.STATE, state)
                .queryParam(OpenIdConstants.Params.NONCE, nonce)
                .queryParam(OpenIdConstants.Params.REDIRECT_URI, softwareStatement.getRedirectUri());
    }

    public void attachAuthFilter(OAuth2Token token) {
        Preconditions.checkState(Objects.isNull(authFilter), "Auth filter cannot be attached twice.");
        authFilter = new OpenIdAuthenticatedHttpFilter(
                token,
                providerConfiguration,
                null,
                null
        );

        httpClient.addFilter(authFilter);
    }

    public void detachAuthFilter() {
        Preconditions.checkNotNull(authFilter, "Auth filter must be attach before it can be detached.");
        try {
            httpClient.removeFilter(authFilter);
        } finally {
            authFilter = null;
        }
    }

    public static String registerClient(SoftwareStatement softwareStatement, URL wellKnownURL,
            TinkHttpClient httpClient) {

        WellKnownResponse wellKnownResponse = httpClient.request(wellKnownURL).get(WellKnownResponse.class);
        URL registrationEndpoint = wellKnownResponse.getRegistrationEndpoint();
        String postData = ClientRegistration.create()
                .withSoftwareStatement(softwareStatement)
                .withWellknownConfiguration(wellKnownResponse)
                .withAccountsScope()
                .withPaymentsScope()
                .build();

        return httpClient.request(registrationEndpoint)
                .type("application/jwt")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(String.class, postData);
    }
}
