package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.ClientMode;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.TOKEN_ENDPOINT_AUTH_METHOD;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.error.OpenIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.JsonWebKeySet;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.TokenRequestForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OpenIdApiClient {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected final TinkHttpClient httpClient;
    protected final SoftwareStatementAssertion softwareStatement;
    protected final String redirectUrl;
    protected final ClientInfo providerConfiguration;
    protected final JwtSigner signer;
    private final URL wellKnownURL;
    private final RandomValueGenerator randomValueGenerator;

    // Internal caching. Do not use these fields directly, always use the getters!
    private WellKnownResponse cachedWellKnownResponse;
    private Map<String, PublicKey> cachedJwkPublicKeys;
    private OpenIdAuthenticatedHttpFilter aisAuthFilter;
    private OpenIdAuthenticatedHttpFilter pisAuthFilter;
    private OpenIdError openIdError;

    public OpenIdApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            String redirectUrl,
            ClientInfo clientInfo,
            URL wellKnownURL,
            RandomValueGenerator randomValueGenerator) {
        this.httpClient = httpClient;
        this.softwareStatement = softwareStatement;
        this.redirectUrl = redirectUrl;
        this.providerConfiguration = clientInfo;
        this.wellKnownURL = wellKnownURL;
        this.signer = signer;
        this.randomValueGenerator = randomValueGenerator;
    }

    public WellKnownResponse getWellKnownConfiguration() {
        if (Objects.nonNull(cachedWellKnownResponse)) {
            return cachedWellKnownResponse;
        }

        /*
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

    public ClientInfo getProviderConfiguration() {
        return providerConfiguration;
    }

    public void addFilter(Filter filter) {
        httpClient.addFilter(filter);
    }

    private TokenRequestForm createTokenRequestFormWithoutScope(String grantType) {
        WellKnownResponse wellknownConfiguration = getWellKnownConfiguration();

        TokenRequestForm requestForm =
                new TokenRequestForm().withGrantType(grantType).withRedirectUri(redirectUrl);

        handleFormAuthentication(requestForm, wellknownConfiguration);

        return requestForm;
    }

    protected void handleFormAuthentication(
            TokenRequestForm requestForm, WellKnownResponse wellknownConfiguration) {
        TOKEN_ENDPOINT_AUTH_METHOD authMethod =
                determineTokenEndpointAuthMethod(providerConfiguration, wellknownConfiguration);

        switch (authMethod) {
            case client_secret_post:
                requestForm.withClientSecretPost(
                        providerConfiguration.getClientId(),
                        providerConfiguration.getClientSecret());
                break;

            case private_key_jwt:
                requestForm.withPrivateKeyJwt(
                        signer, wellknownConfiguration, providerConfiguration);
                break;

            case client_secret_basic:
                // Add to header.
                break;

            case tls_client_auth:
                // Do nothing. We authenticate using client certificate.
                requestForm.withClientId(providerConfiguration.getClientId());
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

    protected RequestBuilder createTokenRequest() {
        WellKnownResponse wellKnownConfiguration = getWellKnownConfiguration();

        RequestBuilder requestBuilder =
                httpClient
                        .request(wellKnownConfiguration.getTokenEndpoint())
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        TOKEN_ENDPOINT_AUTH_METHOD authMethod =
                determineTokenEndpointAuthMethod(providerConfiguration, wellKnownConfiguration);

        switch (authMethod) {
            case client_secret_basic:
                // `client_secret_basic` does not add data to the body, but on the header.
                requestBuilder =
                        requestBuilder.addBasicAuth(
                                providerConfiguration.getClientId(),
                                providerConfiguration.getClientSecret());
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

        String responseType = String.join(" ", OpenIdConstants.MANDATORY_RESPONSE_TYPES);

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
                        .orElse(wellknownConfiguration.getAuthorizationEndpoint());

        /*  'response_type=id_token' only supports 'response_mode=fragment',
         *  setting 'response_mode=query' has no effect the the moment.
         */
        return authorizationEndpoint
                .queryParam(OpenIdConstants.Params.RESPONSE_TYPE, responseType)
                .queryParam(OpenIdConstants.Params.CLIENT_ID, providerConfiguration.getClientId())
                .queryParam(OpenIdConstants.Params.SCOPE, scope)
                .queryParam(OpenIdConstants.Params.STATE, state)
                .queryParam(OpenIdConstants.Params.NONCE, nonce)
                .queryParam(OpenIdConstants.Params.REDIRECT_URI, redirectUri);
    }

    public void instantiateAisAuthFilter(OAuth2Token token) {
        logger.debug("Instantiating the Ais Auth Filter.");
        aisAuthFilter = new OpenIdAuthenticatedHttpFilter(token, randomValueGenerator);
    }

    public void instantiatePisAuthFilter(OAuth2Token token) {
        logger.debug("Instantiating the Pis Auth Filter.");
        pisAuthFilter = new OpenIdAuthenticatedHttpFilter(token, randomValueGenerator);
    }

    public void storeOpenIdError(OpenIdError error) {
        openIdError = error;
    }

    public Optional<OpenIdError> getOpenIdError() {
        return Optional.ofNullable(openIdError);
    }

    public Optional<Map<String, PublicKey>> getJwkPublicKeys() {
        if (Objects.nonNull(cachedJwkPublicKeys)) {
            return Optional.of(cachedJwkPublicKeys);
        }

        String response =
                httpClient.request(getWellKnownConfiguration().getJwksUri()).get(String.class);

        JsonWebKeySet jsonWebKeySet =
                SerializationUtils.deserializeFromString(response, JsonWebKeySet.class);

        if (jsonWebKeySet == null) {
            return Optional.empty();
        }

        cachedJwkPublicKeys = jsonWebKeySet.getAllKeysMap();
        return Optional.ofNullable(cachedJwkPublicKeys);
    }
}
