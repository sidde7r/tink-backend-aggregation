package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import com.google.common.base.Strings;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Scopes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.TokenEndpointAuthMethod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.JsonWebKeySet;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.TokenRequestForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BadGatewayFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public class OpenIdApiClient {

    protected final TinkHttpClient httpClient;
    @Getter protected final SoftwareStatementAssertion softwareStatement;
    @Getter protected final String redirectUrl;
    @Getter protected final ClientInfo providerConfiguration;
    @Getter protected final JwtSigner signer;
    private final URL wellKnownURL;
    private final RandomValueGenerator randomValueGenerator;

    // Internal caching. Do not use these fields directly, always use the getters!
    private WellKnownResponse cachedWellKnownResponse;
    private Map<String, PublicKey> cachedJwkPublicKeys;

    @Getter @Setter private OpenIdAuthenticatedHttpFilter aisAuthFilter;

    @Getter private OpenIdAuthenticatedHttpFilter pisAuthFilter;

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
        this.httpClient.addFilter(new BankServiceInternalErrorFilter());
        this.httpClient.addFilter(new BadGatewayFilter());
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

    public URL buildAuthorizeUrl(String state, String nonce, ClientMode mode, String callbackUri) {
        WellKnownResponse wellKnownConfiguration = getWellKnownConfiguration();

        URL authorizationEndpointUrl = wellKnownConfiguration.getAuthorizationEndpoint();

        List<String> requiredScopes = createRequiredScopeList(mode, wellKnownConfiguration);
        String responseType = wellKnownConfiguration.getResponseType();
        String clientId = providerConfiguration.getClientId();

        String scopeArray =
                wellKnownConfiguration
                        .verifyAndGetScopes(requiredScopes)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "[OpenIdApiClient] Provider does not support required scopes: "
                                                        + String.join(" ", requiredScopes)));
        String redirectUri =
                Optional.ofNullable(callbackUri).filter(s -> !s.isEmpty()).orElse(redirectUrl);

        /*  'response_type=id_token' only supports 'response_mode=fragment',
         *  setting 'response_mode=query' has no effect the the moment.
         */
        return authorizationEndpointUrl
                .queryParam(OpenIdConstants.Params.RESPONSE_TYPE, responseType)
                .queryParam(OpenIdConstants.Params.CLIENT_ID, clientId)
                .queryParam(OpenIdConstants.Params.SCOPE, scopeArray)
                .queryParam(OpenIdConstants.Params.STATE, state)
                .queryParam(OpenIdConstants.Params.NONCE, nonce)
                .queryParam(OpenIdConstants.Params.REDIRECT_URI, redirectUri);
    }

    public void instantiateAisAuthFilter(OAuth2Token token) {
        log.debug("Instantiating the Ais Auth Filter.");
        aisAuthFilter = new OpenIdAuthenticatedHttpFilter(token, randomValueGenerator);
    }

    public void instantiatePisAuthFilter(OAuth2Token token) {
        log.debug("Instantiating the Pis Auth Filter.");
        pisAuthFilter = new OpenIdAuthenticatedHttpFilter(token, randomValueGenerator);
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

    protected RequestBuilder createBasicTokenRequest(WellKnownResponse wellKnownConfiguration) {
        return httpClient
                .request(wellKnownConfiguration.getTokenEndpoint())
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    protected void handleFormAuthentication(
            TokenRequestForm requestForm, WellKnownResponse wellKnownConfiguration) {
        final TokenEndpointAuthMethod authMethod =
                determineTokenEndpointAuthMethod(providerConfiguration, wellKnownConfiguration);

        switch (authMethod) {
            case CLIENT_SECRET_POST:
                requestForm.withClientSecretPost(
                        providerConfiguration.getClientId(),
                        providerConfiguration.getClientSecret());
                break;

            case PRIVATE_KEY_JWT:
                requestForm.withPrivateKeyJwt(
                        signer, wellKnownConfiguration, providerConfiguration);
                break;

            case CLIENT_SECRET_BASIC:
                // Add to header.
                break;

            case TLS_CLIENT_AUTH:
                // Do nothing. We authenticate using client certificate.
                requestForm.withClientId(providerConfiguration.getClientId());
                break;

            default:
                throw new IllegalStateException(
                        String.format(
                                "Not yet implemented auth method: %s", authMethod.toString()));
        }
    }

    protected TokenRequestForm createTokenRequestForm(String grantType, ClientMode mode) {
        WellKnownResponse wellKnownConfiguration = getWellKnownConfiguration();

        // Token request does not use OpenId scope
        String scope =
                wellKnownConfiguration
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

        handleFormAuthentication(requestForm, wellKnownConfiguration);

        return requestForm;
    }

    protected RequestBuilder createTokenRequest() {
        WellKnownResponse wellKnownConfiguration = getWellKnownConfiguration();

        RequestBuilder requestBuilder = createBasicTokenRequest(wellKnownConfiguration);

        TokenEndpointAuthMethod authMethod =
                determineTokenEndpointAuthMethod(providerConfiguration, wellKnownConfiguration);

        switch (authMethod) {
            case CLIENT_SECRET_BASIC:
                // `client_secret_basic` does not add data to the body, but on the header.
                requestBuilder =
                        requestBuilder.addBasicAuth(
                                providerConfiguration.getClientId(),
                                providerConfiguration.getClientSecret());
                break;
            case TLS_CLIENT_AUTH:
                break;

            case PRIVATE_KEY_JWT:
                // Add to header.
                break;

            case CLIENT_SECRET_POST:
                // Do nothing. We authenticate using client certificate.
                break;

            default:
                throw new IllegalStateException(
                        String.format(
                                "Not yet implemented auth method: %s", authMethod.toString()));
        }

        return requestBuilder;
    }

    private TokenEndpointAuthMethod determineTokenEndpointAuthMethod(
            ClientInfo clientInfo, WellKnownResponse wellKnownConfiguration) {

        if (!Strings.isNullOrEmpty(clientInfo.getTokenEndpointAuthMethod())) {
            return TokenEndpointAuthMethod.valueOf(
                    clientInfo.getTokenEndpointAuthMethod().toUpperCase());
        }
        log.info(
                "[OpenIdApiClient]: Entering second return statement for TokenEndpointAuthMethod."
                        + "Getting TokenEndpointAuthMethod from wellKnownConfiguration or throwing exception.");
        return wellKnownConfiguration
                .getPreferredTokenEndpointAuthMethod(
                        OpenIdConstants.PREFERRED_TOKEN_ENDPOINT_AUTH_METHODS)
                .orElseThrow(
                        () ->
                                AuthorizationError.UNAUTHORIZED.exception(
                                        "[OpenIdApiClient]: Preferred token endpoint auth method not found."));
    }

    private TokenRequestForm createTokenRequestFormWithoutScope(String grantType) {
        WellKnownResponse wellknownConfiguration = getWellKnownConfiguration();

        TokenRequestForm requestForm =
                new TokenRequestForm().withGrantType(grantType).withRedirectUri(redirectUrl);

        handleFormAuthentication(requestForm, wellknownConfiguration);

        return requestForm;
    }

    private List<String> createRequiredScopeList(
            ClientMode mode, WellKnownResponse wellKnownConfiguration) {
        if (wellKnownConfiguration.isOfflineAccessSupported()) {
            return Arrays.asList(Scopes.OPEN_ID, mode.getValue(), Scopes.OFFLINE_ACCESS);
        }
        return Arrays.asList(Scopes.OPEN_ID, mode.getValue());
    }
}
