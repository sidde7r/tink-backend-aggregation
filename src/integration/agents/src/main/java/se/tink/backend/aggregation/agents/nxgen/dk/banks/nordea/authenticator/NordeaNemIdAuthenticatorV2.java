package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.AuthenticationsPatchResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.NemIdParamsResponse;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParameters;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParametersFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.Catalog;

public class NordeaNemIdAuthenticatorV2
        implements MultiFactorAuthenticator, AutoAuthenticator, NemIdParametersFetcher {

    private static final String INVALID_GRANT = "invalid_grant";
    private static final String NEM_ID_SCRIPT_FORMAT =
            "<script type=\"text/x-nemid\" id=\"nemid_parameters\">%s</script>";
    private static final Logger log = LoggerFactory.getLogger(NordeaNemIdAuthenticatorV2.class);
    private final NordeaDkApiClient bankClient;
    private final NemIdIFrameController iFrameController;

    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public NordeaNemIdAuthenticatorV2(
            final NordeaDkApiClient bankClient,
            final SessionStorage sessionStorage,
            final PersistentStorage persistentStorage,
            final Catalog catalog,
            final StatusUpdater statusUpdater,
            final SupplementalInformationController supplementalInformationController,
            final MetricContext metricContext) {
        this.bankClient = Objects.requireNonNull(bankClient);
        this.sessionStorage = Objects.requireNonNull(sessionStorage);
        this.persistentStorage = Objects.requireNonNull(persistentStorage);
        this.iFrameController =
                NemIdIFrameControllerInitializer.initNemIdIframeController(
                        this,
                        catalog,
                        statusUpdater,
                        supplementalInformationController,
                        metricContext);
    }

    public void authenticate(final Credentials credentials) throws AuthenticationException {
        if (Strings.isNullOrEmpty(credentials.getField(Field.Key.PASSWORD))
                || Strings.isNullOrEmpty(credentials.getField(Field.Key.USERNAME))) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        String token = iFrameController.logInWithCredentials(credentials);
        final String code = exchangeNemIdToken(token);
        saveToken(exchangeOauthToken(code));
    }

    public void autoAuthenticate() {
        try {
            OAuth2Token token =
                    getToken().orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
            String refreshToken =
                    token.getRefreshToken()
                            .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
            OAuth2Token newToken =
                    bankClient
                            .exchangeRefreshToken(refreshToken)
                            .toOauthToken()
                            .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
            saveToken(newToken);
        } catch (HttpResponseException e) {
            String error = (String) e.getResponse().getBody(Map.class).get("error");
            if (e.getResponse().getStatus() == 400 && INVALID_GRANT.equals(error)) {
                // refresh token expired
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw e;
        } catch (AuthenticationException e) {
            log.info("Refresh token missing or invalid, proceeding to manual authentication");
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public NemIdParameters getNemIdParameters() {
        String codeVerifier = generateCodeVerifier();
        sessionStorage.put(StorageKeys.CODE_VERIFIER, codeVerifier);
        String state = generateState();
        String nonce = generateNonce();
        sessionStorage.put(StorageKeys.NONCE, nonce);
        String referer = bankClient.initOauth(generateCodeChallenge(codeVerifier), state, nonce);
        sessionStorage.put(StorageKeys.REFERER, referer);
        NemIdParamsResponse nemIdParamsResponse =
                bankClient.getNemIdParams(generateCodeChallenge(codeVerifier), state, nonce);
        String sessionId = nemIdParamsResponse.getSessionId();
        sessionStorage.put(StorageKeys.SESSION_ID, sessionId);
        ObjectMapper mapper = new ObjectMapper();
        String params;
        try {
            params = mapper.writeValueAsString(nemIdParamsResponse.getNemidParams());
        } catch (JsonProcessingException e) {
            throw NemIdError.INTERRUPTED.exception();
        }
        return new NemIdParameters(
                String.format(NEM_ID_SCRIPT_FORMAT, params)
                        + String.format(
                                NemIdConstants.NEM_ID_IFRAME_FORMAT,
                                NemIdConstants.NEM_ID_INIT_URL + Instant.now().toEpochMilli()));
    }

    private String exchangeNemIdToken(String nemIdToken) {
        String referer = sessionStorage.get(StorageKeys.REFERER);
        AuthenticationsPatchResponse authenticationsPatchResponse =
                bankClient.authenticationsPatch(
                        nemIdToken, sessionStorage.get(StorageKeys.SESSION_ID), referer);
        sessionStorage.put(StorageKeys.NEMID_TOKEN, authenticationsPatchResponse.getNemIdToken());
        String code = authenticationsPatchResponse.getCode();
        return bankClient.codeExchange(code, referer).getCode();
    }

    private OAuth2Token exchangeOauthToken(String installId) throws AuthenticationException {
        return bankClient
                .oauthCallback(installId, sessionStorage.get(StorageKeys.CODE_VERIFIER))
                .toOauthToken()
                .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
    }

    private String generateCodeVerifier() {
        return RandomUtils.generateRandomBase64UrlEncoded(86);
    }

    private String generateCodeChallenge(String codeVerifier) {
        byte[] digest = Hash.sha256(codeVerifier);
        return EncodingUtils.encodeAsBase64UrlSafe(digest);
    }

    private String generateNonce() {
        return RandomUtils.generateRandomBase64UrlEncoded(26);
    }

    private String generateState() {
        return RandomUtils.generateRandomBase64UrlEncoded(26);
    }

    private void saveToken(OAuth2Token token) {
        this.persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);
        this.sessionStorage.put(StorageKeys.OAUTH_TOKEN, token);
    }

    private Optional<OAuth2Token> getToken() {
        return this.persistentStorage.get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class);
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }
}
