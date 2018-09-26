package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.google.common.collect.ImmutableList;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.Random;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.common.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.i18n.LocalizableKey;

public class OpenIdAuthenticationController implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private static final Random random = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder();
    private static final ImmutableList<String> RESPONSE_TYPES = ImmutableList.<String>builder()
            .add("code")
            .add("id_token")
            .build();

    private final OpenIdAuthenticator authenticator;
    private final SoftwareStatement softwareStatement;
    private final OpenIdApiClient apiClient;
    private final String state;

    public OpenIdAuthenticationController(TinkHttpClient httpClient, OpenIdAuthenticator authenticator) {
        this.authenticator = authenticator;
        this.softwareStatement = authenticator.getClientConfiguration();
        this.apiClient = new OpenIdApiClient(httpClient, softwareStatement, null);
        this.state = generateRandomState();
    }

    private static String generateRandomState() {
        byte[] randomData = new byte[32];
        random.nextBytes(randomData);
        return encoder.encodeToString(randomData);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        throw SessionError.SESSION_EXPIRED.exception();
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        return null;
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) throws AuthenticationException,
            AuthorizationException {
        return null;
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        URL authorizationEndpoint = apiClient.getAuthorizationEndpoint();

        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();

        ThirdPartyAppAuthenticationPayload.Android androidPayload = new ThirdPartyAppAuthenticationPayload.Android();
        androidPayload.setIntent(authorizationEndpoint.get());
        payload.setAndroid(androidPayload);

        ThirdPartyAppAuthenticationPayload.Ios iOsPayload = new ThirdPartyAppAuthenticationPayload.Ios();
        iOsPayload.setAppScheme(authorizationEndpoint.getScheme());
        iOsPayload.setDeepLinkUrl(authorizationEndpoint.get());
        payload.setIos(iOsPayload);

        return null;
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    private void xxx() {



    }

    private URL buildAuthorizationRequest(URL authorizationEndpoint) {
        WellKnownResponse providerConfiguration = apiClient.getProviderConfiguration();
        String responseType = providerConfiguration.verifyAndGetResponseTypes(RESPONSE_TYPES)
                .orElseThrow(
                        () -> new IllegalStateException(
                                String.format(
                                        "NYI, response types not supported: %s",
                                        providerConfiguration
                                )
                        )
                );

        String scope = providerConfiguration.verifyAndGetScopes(OpenIdConstants.SCOPES)
                .orElseThrow(
                        () -> new IllegalStateException(
                                String.format(
                                        "NYI, scopes not supported: %s",
                                        providerConfiguration
                                )
                        )
                );

        /*
        authorizationEndpoint = authorizationEndpoint
                .queryParam("response_type", responseType)
                .queryParam("client_id", clientConfiguration.getClientId())
                .queryParam("redirect_uri", clientConfiguration.getRedirectUri())
                .queryParam("scope", scope)
                .queryParam("state", state);


        JWTCreator.Builder requestSignatureHeader = JWT.create()
                .withIssuedAt(new Date())
                .withIssuer(clientConfiguration.getClientId())
                .withAudience(providerConfiguration.getIssuer())
                .withClaim("response_type", responseType)
                .withClaim("client_id", clientConfiguration.getClientId())
                .withClaim("redirect_uri", clientConfiguration.getRedirectUri())
                .withClaim("scope", scope)
                .withClaim("state", state);


        if (providerConfiguration.isRequestParameterSupported()) {
            authorizationEndpoint = authorizationEndpoint
                    .queryParam("request", "");
        }
           */
        return authorizationEndpoint;
    }
}
