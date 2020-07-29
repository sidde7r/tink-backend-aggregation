package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.ClientMode;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.TOKEN_ENDPOINT_AUTH_METHOD;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.error.OpenIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.ClientRegistration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.JsonWebKeySet;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.TokenRequestForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OpenIdApiClient {

    protected final TinkHttpClient httpClient;
    protected final SoftwareStatementAssertion softwareStatement;
    protected final String redirectUrl;
    protected final ProviderConfiguration providerConfiguration;
    protected final JwtSigner signer;
    private final URL wellKnownURL;
    private final RandomValueGenerator randomValueGenerator;

    // Internal caching. Do not use these fields directly, always use the getters!
    private WellKnownResponse cachedWellKnownResponse;
    private JsonWebKeySet cachedProviderKeys;
    private OpenIdAuthenticatedHttpFilter aisAuthFilter;
    private OpenIdAuthenticatedHttpFilter pisAuthFilter;
    private OpenIdError openIdError;
    private static final AggregationLogger logger = new AggregationLogger(OpenIdApiClient.class);

    public OpenIdApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            String redirectUrl,
            ProviderConfiguration providerConfiguration,
            URL wellKnownURL,
            RandomValueGenerator randomValueGenerator) {
        this.httpClient = httpClient;
        this.softwareStatement = softwareStatement;
        this.redirectUrl = redirectUrl;
        this.providerConfiguration = providerConfiguration;
        this.wellKnownURL = wellKnownURL;
        this.signer = signer;
        this.randomValueGenerator = randomValueGenerator;
    }

    public WellKnownResponse getWellKnownConfiguration() {
        if (Objects.nonNull(cachedWellKnownResponse)) {
            return cachedWellKnownResponse;
        }

        /**
         * Regarding the well-known URL endpoint, some bank APIs (such as FirstDirect) sends
         * response with wrong MIME type (such as octet-stream). If we want to cast the response
         * payload into WellKnownResponse directly, we fail as TinkHttpClient does not know how to
         * handle application/octet-stream in this case. For this reason, we cast the response
         * payload into string first and then serialize it by using SerializationUtils class
         */
        String response = httpClient.request(wellKnownURL).get(String.class);

        cachedWellKnownResponse =
                SerializationUtils.deserializeFromString(response, WellKnownResponse.class);

        return cachedWellKnownResponse;
    }

    public OpenIdAuthenticatedHttpFilter getAisAuthFilter() {
        return aisAuthFilter;
    }

    public OpenIdAuthenticatedHttpFilter getPisAuthFilter() {
        return pisAuthFilter;
    }

    public SoftwareStatementAssertion getSoftwareStatement() {
        return softwareStatement;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public JwtSigner getSigner() {
        return signer;
    }

    public ProviderConfiguration getProviderConfiguration() {
        return providerConfiguration;
    }

    public JsonWebKeySet getProviderKeys() {
        if (Objects.nonNull(cachedProviderKeys)) {
            return cachedProviderKeys;
        }

        WellKnownResponse providerConfiguration = getWellKnownConfiguration();
        cachedProviderKeys =
                httpClient.request(providerConfiguration.getJwksUri()).get(JsonWebKeySet.class);

        return cachedProviderKeys;
    }

    private TokenRequestForm createTokenRequestFormWithoutScope(String grantType) {
        WellKnownResponse wellknownConfiguration = getWellKnownConfiguration();

        TokenRequestForm requestForm =
                new TokenRequestForm().withGrantType(grantType).withRedirectUri(redirectUrl);

        handleFormAuthentication(requestForm, wellknownConfiguration);

        return requestForm;
    }

    private void handleFormAuthentication(
            TokenRequestForm requestForm, WellKnownResponse wellknownConfiguration) {

        ClientInfo clientInfo = providerConfiguration.getClientInfo();
        TOKEN_ENDPOINT_AUTH_METHOD authMethod =
                determineTokenEndpointAuthMethod(clientInfo, wellknownConfiguration);

        switch (authMethod) {
            case client_secret_post:
                requestForm.withClientSecretPost(
                        clientInfo.getClientId(), clientInfo.getClientSecret());
                break;

            case private_key_jwt:
                requestForm.withPrivateKeyJwt(signer, wellknownConfiguration, clientInfo);
                break;

            case client_secret_basic:
                // Add to header.
                break;

            case tls_client_auth:
                // Do nothing. We authenticate using client certificate.
                requestForm.withClientId(clientInfo.getClientId());
                break;

            default:
                throw new IllegalStateException(
                        String.format(
                                "Not yet implemented auth method: %s", authMethod.toString()));
        }
    }

    private TokenRequestForm createTokenRequestForm(String grantType, ClientMode mode) {
        WellKnownResponse wellknownConfiguration = getWellKnownConfiguration();

        // Token request does not use OpenId scope
        String scope =
                wellknownConfiguration
                        .verifyAndGetScopes(Collections.singletonList(mode.getValue()))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Provider does not support the mandatory scopes."));

        TokenRequestForm requestForm =
                new TokenRequestForm()
                        .withGrantType(grantType)
                        .withScope(scope)
                        .withRedirectUri(redirectUrl);

        handleFormAuthentication(requestForm, wellknownConfiguration);

        return requestForm;
    }

    private RequestBuilder createTokenRequest() {
        WellKnownResponse wellknownConfiguration = getWellKnownConfiguration();

        RequestBuilder requestBuilder =
                httpClient
                        .request(wellknownConfiguration.getTokenEndpoint())
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        ClientInfo clientInfo = providerConfiguration.getClientInfo();

        TOKEN_ENDPOINT_AUTH_METHOD authMethod =
                determineTokenEndpointAuthMethod(clientInfo, wellknownConfiguration);

        switch (authMethod) {
            case client_secret_basic:
                // `client_secret_basic` does not add data to the body, but on the header.
                requestBuilder =
                        requestBuilder.addBasicAuth(
                                clientInfo.getClientId(), clientInfo.getClientSecret());
                break;
            case tls_client_auth:
                break;

            case private_key_jwt:
                // Add to header.
                break;

            case client_secret_post:
                // Do nothing. We authenticate using client certificate.
                break;

            default:
                throw new IllegalStateException(
                        String.format(
                                "Not yet implemented auth method: %s", authMethod.toString()));
        }

        return requestBuilder;
    }

    private TOKEN_ENDPOINT_AUTH_METHOD determineTokenEndpointAuthMethod(
            ClientInfo clientInfo, WellKnownResponse wellknownConfiguration) {

        if (!Strings.isNullOrEmpty(clientInfo.getTokenEndpointAuthMethod())) {
            return TOKEN_ENDPOINT_AUTH_METHOD.valueOf(clientInfo.getTokenEndpointAuthMethod());
        }

        return wellknownConfiguration
                .getPreferredTokenEndpointAuthMethod(
                        OpenIdConstants.PREFERRED_TOKEN_ENDPOINT_AUTH_METHODS)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Preferred token endpoint auth method not found."));
    }

    public OAuth2Token requestClientCredentials(ClientMode scope) {
        TokenRequestForm postData = createTokenRequestForm("client_credentials", scope);

        return createTokenRequest().body(postData).post(TokenResponse.class).toAccessToken();
    }

    public OAuth2Token refreshAccessToken(String refreshToken, ClientMode scope) {
        TokenRequestForm postData =
                createTokenRequestForm("refresh_token", scope).withRefreshToken(refreshToken);

        return createTokenRequest().body(postData).post(TokenResponse.class).toAccessToken();
    }

    public OAuth2Token exchangeAccessCode(String code) {
        TokenRequestForm postData =
                createTokenRequestFormWithoutScope("authorization_code").withCode(code);

        return createTokenRequest().body(postData).post(TokenResponse.class).toAccessToken();
    }

    public URL buildAuthorizeUrl(
            String state, String nonce, ClientMode mode, String callbackUri, URL authEndpoint) {
        WellKnownResponse wellknownConfiguration = getWellKnownConfiguration();
        ClientInfo clientInfo = providerConfiguration.getClientInfo();

        String responseType =
                OpenIdConstants.MANDATORY_RESPONSE_TYPES.stream().collect(Collectors.joining(" "));

        String scope =
                wellknownConfiguration
                        .verifyAndGetScopes(
                                Arrays.asList(OpenIdConstants.Scopes.OPEN_ID, mode.getValue()))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Provider does not support the mandatory scopes."));

        String redirectUri =
                Optional.ofNullable(callbackUri).filter(s -> !s.isEmpty()).orElse(redirectUrl);

        URL authorizationEndpoint =
                Optional.ofNullable(authEndpoint)
                        .filter(s -> s != null)
                        .orElse(wellknownConfiguration.getAuthorizationEndpoint());

        /*  'response_type=id_token' only supports 'response_mode=fragment',
         *  setting 'response_mode=query' has no effect the the moment.
         */
        return authorizationEndpoint
                .queryParam(OpenIdConstants.Params.RESPONSE_TYPE, responseType)
                .queryParam(OpenIdConstants.Params.CLIENT_ID, clientInfo.getClientId())
                .queryParam(OpenIdConstants.Params.SCOPE, scope)
                .queryParam(OpenIdConstants.Params.STATE, state)
                .queryParam(OpenIdConstants.Params.NONCE, nonce)
                .queryParam(OpenIdConstants.Params.REDIRECT_URI, redirectUri);
    }

    public void instantiateAisAuthFilter(OAuth2Token token) {
        logger.debug("Instantiating the Ais Auth Filter.");
        aisAuthFilter =
                new OpenIdAuthenticatedHttpFilter(
                        token, providerConfiguration, null, null, randomValueGenerator);
    }

    public void instantiatePisAuthFilter(OAuth2Token token) {
        logger.debug("Instantiating the Pis Auth Filter.");
        pisAuthFilter =
                new OpenIdAuthenticatedHttpFilter(
                        token, providerConfiguration, null, null, randomValueGenerator);
    }

    public void storeOpenIdError(OpenIdError error) {
        openIdError = error;
    }

    public Optional<OpenIdError> getOpenIdError() {
        return Optional.ofNullable(openIdError);
    }

    public static String registerClient(
            SoftwareStatement softwareStatement,
            URL wellKnownURL,
            TinkHttpClient httpClient,
            JwtSigner signer) {

        WellKnownResponse wellKnownResponse =
                SerializationUtils.deserializeFromString(
                        httpClient.request(wellKnownURL).get(String.class),
                        WellKnownResponse.class);
        URL registrationEndpoint = wellKnownResponse.getRegistrationEndpoint();
        String postData =
                ClientRegistration.create()
                        .withSoftwareStatement(softwareStatement)
                        .withWellknownConfiguration(wellKnownResponse)
                        .withAccountsScope()
                        .withPaymentsScope()
                        .build(signer);

        return httpClient
                .request(registrationEndpoint)
                .type("application/jwt")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(String.class, postData);
    }
}
