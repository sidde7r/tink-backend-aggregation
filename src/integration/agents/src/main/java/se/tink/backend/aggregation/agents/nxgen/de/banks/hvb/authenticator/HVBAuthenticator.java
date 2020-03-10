package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import static se.tink.backend.aggregation.agents.exceptions.errors.LoginError.INCORRECT_CREDENTIALS;
import static se.tink.backend.aggregation.agents.exceptions.errors.LoginError.REGISTER_DEVICE_ERROR;

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
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.DateTimeProvider;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBStorage;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.JwkHeader.Jwk;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.scaffold.SimpleExternalApiCall;

public final class HVBAuthenticator implements Authenticator {

    private final HVBStorage storage;
    private final ConfigurationProvider configurationProvider;
    private final DataEncoder dataEncoder;
    private final DateTimeProvider dateTimeProvider;

    private final RegistrationCall registrationRequest;
    private final PreAuthorizationCall preAuthorizationRequest;
    private final AuthorizationCall authorizationRequest;
    private final AccessTokenCall accessTokenCall;

    public HVBAuthenticator(
            final HVBStorage storage,
            ConfigurationProvider configurationProvider,
            DataEncoder dataEncoder,
            DateTimeProvider dateTimeProvider,
            RegistrationCall registrationRequest,
            PreAuthorizationCall preAuthorizationRequest,
            AuthorizationCall authorizationRequest,
            AccessTokenCall accessTokenCall) {
        this.configurationProvider = configurationProvider;
        this.storage = storage;
        this.dataEncoder = dataEncoder;
        this.dateTimeProvider = dateTimeProvider;
        this.registrationRequest = registrationRequest;
        this.preAuthorizationRequest = preAuthorizationRequest;
        this.authorizationRequest = authorizationRequest;
        this.accessTokenCall = accessTokenCall;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw INCORRECT_CREDENTIALS.exception();
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
        String clientId = executeCall(registrationRequest, registrationData, REGISTER_DEVICE_ERROR);
        storage.setClientId(clientId);
        storage.setKeyPair(registrationData.getKeyPair());
    }

    private AuthenticationData prepareRegistrationData() {
        KeyPair keyPair = configurationProvider.generateRsaKeyPair();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        return AuthenticationData.forRegistration(
                configurationProvider.generateDeviceId(), keyPair, prepareJwkHeader(rsaPrivateKey));
    }

    private <T, R> R executeCall(SimpleExternalApiCall<T, R> call, T arg, LoginError loginErrorType)
            throws LoginException {
        return Optional.ofNullable(call.execute(arg))
                .filter(ExternalApiCallResult::isSuccess)
                .map(ExternalApiCallResult::getResult)
                .orElseThrow(loginErrorType::exception);
    }

    private void authenticateRegisteredClient(String username, String password)
            throws LoginException {

        AuthenticationData authenticationData = preAuthorize(username, password);
        String code = getAuthorizationCode(authenticationData);
        AccessTokenResponse accessToken = getAccessToken(authenticationData, code);

        storage.setAccessToken(accessToken.getValue());
        storage.setDirectBankingNumber(username);
    }

    private AccessTokenResponse getAccessToken(AuthenticationData authenticationData, String code)
            throws LoginException {
        enrichAuthorizationData(authenticationData, code, dateTimeProvider.getInstantNow());
        return executeCall(accessTokenCall, authenticationData, INCORRECT_CREDENTIALS);
    }

    private String getAuthorizationCode(AuthenticationData authenticationData)
            throws LoginException {
        return executeCall(authorizationRequest, authenticationData, INCORRECT_CREDENTIALS);
    }

    private AuthenticationData preAuthorize(String username, String password)
            throws LoginException {
        AuthenticationData authenticationData = prepareAuthorizationData(username, password);
        executeCall(preAuthorizationRequest, authenticationData, INCORRECT_CREDENTIALS);
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
        return new JwkHeader().setJwk(prepareJwk((RSAPrivateCrtKey) rsaPrivateKey));
    }

    private Jwk prepareJwk(RSAPrivateCrtKey key) {
        return new Jwk()
                .setExponent(dataEncoder.base64Encode(key.getPublicExponent().toByteArray()))
                .setModulus(dataEncoder.base64Encode(key.getModulus().toByteArray()));
    }
}
