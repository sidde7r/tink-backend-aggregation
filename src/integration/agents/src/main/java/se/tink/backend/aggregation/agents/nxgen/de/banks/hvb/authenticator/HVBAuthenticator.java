package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import com.google.common.base.Strings;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBStorage;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.JwkHeader.Jwk;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.scaffold.SimpleExternalApiCall;

public final class HVBAuthenticator implements Authenticator {
    private final HVBStorage storage;
    private final ConfigurationProvider configurationProvider;
    private final DataEncoder dataEncoder;

    private final RegistrationRequest registrationRequest;
    private final PreAuthorizationRequest preAuthorizationRequest;
    private final AuthorizationRequest authorizationRequest;
    private final AccessTokenRequest accessTokenRequest;

    public HVBAuthenticator(
            final HVBStorage storage,
            ConfigurationProvider configurationProvider,
            DataEncoder dataEncoder,
            RegistrationRequest registrationRequest,
            PreAuthorizationRequest preAuthorizationRequest,
            AuthorizationRequest authorizationRequest,
            AccessTokenRequest accessTokenRequest) {
        this.configurationProvider = configurationProvider;
        this.storage = storage;
        this.dataEncoder = dataEncoder;
        this.registrationRequest = registrationRequest;
        this.preAuthorizationRequest = preAuthorizationRequest;
        this.authorizationRequest = authorizationRequest;
        this.accessTokenRequest = accessTokenRequest;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        authenticate(username, password);
    }

    private void authenticate(final String username, final String password)
            throws AuthenticationException {

        if (isNotRegisteredClient()) {
            register();
        }
        authenticateRegisteredClient(username, password);
    }

    private boolean isNotRegisteredClient() {
        return Objects.isNull(storage.getClientId());
    }

    private void register() throws AuthenticationException {

        AuthenticationData registrationData = prepareRegistrationData();
        String clientId = executeCall(registrationRequest, registrationData);
        storage.setClientId(clientId);
        storage.setKeyPair(registrationData.getKeyPair());
    }

    private AuthenticationData prepareRegistrationData() {
        KeyPair keyPair = configurationProvider.generateRsaKeyPair();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        return AuthenticationData.forRegistration(
                configurationProvider.generateDeviceId(), keyPair, prepareJwkHeader(rsaPrivateKey));
    }

    private <T, R> R executeCall(SimpleExternalApiCall<T, R> call, T arg) throws LoginException {
        return Optional.ofNullable(call.execute(arg))
                .filter(ExternalApiCallResult::isSuccess)
                .map(ExternalApiCallResult::getResult)
                .orElseThrow(LoginError.INCORRECT_CREDENTIALS::exception);
    }

    private void authenticateRegisteredClient(String username, String password)
            throws LoginException {

        AuthenticationData authenticationData = preAuthorize(username, password);
        String code = getAuthorizationCode(authenticationData);
        AccessToken accessToken = getAccessToken(authenticationData, code);

        storage.setAccessToken(accessToken);
    }

    private AccessToken getAccessToken(AuthenticationData authenticationData, String code)
            throws LoginException {
        enrichAuthorizationData(authenticationData, code, Instant.now());
        return executeCall(accessTokenRequest, authenticationData);
    }

    private String getAuthorizationCode(AuthenticationData authenticationData)
            throws LoginException {
        return executeCall(authorizationRequest, authenticationData);
    }

    private AuthenticationData preAuthorize(String username, String password)
            throws LoginException {
        AuthenticationData authenticationData = prepareAuthorizationData(username, password);
        executeCall(preAuthorizationRequest, authenticationData);
        return authenticationData;
    }

    private AuthenticationData prepareAuthorizationData(String username, String password) {
        String clientId = storage.getClientId();
        KeyPair keyPair = storage.getKeyPair();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        return AuthenticationData.forAuthorization(
                clientId,
                username,
                password,
                keyPair,
                prepareJwkHeader(rsaPrivateKey),
                configurationProvider.generateApplicationSessionId());
    }

    private AuthenticationData enrichAuthorizationData(
            AuthenticationData authenticationData, String code, Instant instant) {
        return authenticationData.setCode(code).setInstant(instant);
    }

    private JwkHeader prepareJwkHeader(RSAPrivateKey rsaPrivateKey) {
        return new JwkHeader().setAlg("RS256").setJwk(prepareJwk((RSAPrivateCrtKey) rsaPrivateKey));
    }

    private Jwk prepareJwk(RSAPrivateCrtKey rsaPrivateCrtKey) {
        return new Jwk()
                .setKty("RSA")
                .setE(dataEncoder.base64Encode(rsaPrivateCrtKey.getPublicExponent().toByteArray()))
                .setN(dataEncoder.base64Encode(rsaPrivateCrtKey.getModulus().toByteArray()));
    }
}
