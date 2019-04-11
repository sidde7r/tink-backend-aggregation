package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.entities.BpiMetaData;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.storage.LclPersistentStorage;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LclAuthenticator implements PasswordAuthenticator {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final LclApiClient apiClient;
    private final LclPersistentStorage lclPersistentStorage;

    public LclAuthenticator(LclApiClient apiClient, LclPersistentStorage lclPersistentStorage) {
        this.apiClient = apiClient;
        this.lclPersistentStorage = lclPersistentStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        configureDeviceIfNotConfigured();
        String agentKey = getAgentKeyForSessionId();
        String sessionId = generateSessionId(agentKey);

        BpiMetaData bpiMetaDataEntity = BpiMetaData.create(sessionId);
        String bpiMetaDataB64String =
                EncodingUtils.encodeAsBase64String(
                        Objects.requireNonNull(
                                SerializationUtils.serializeToString(bpiMetaDataEntity)));

        String xorPinB64String = getXorPinInB64(password);

        LoginResponse loginResponse =
                apiClient.login(username, bpiMetaDataB64String, xorPinB64String);

        checkForErrors(loginResponse);
    }

    private void checkForErrors(LoginResponse loginResponse) throws LoginException {
        if (LclConstants.Authentication.ERROR_TRUE.equalsIgnoreCase(loginResponse.getError())) {
            String errorCode = Optional.ofNullable(loginResponse.getErrorCode()).orElse("");

            if (LclConstants.Authentication.INCORRECT_LOGIN_CREDENTIALS.equalsIgnoreCase(errorCode)
                    || LclConstants.Authentication.INCORRECT_PASSWORD.equalsIgnoreCase(errorCode)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            throw new IllegalStateException(
                    String.format(
                            "Could not authenticate user: errorCode %s, errorMessage: %s",
                            errorCode,
                            Optional.ofNullable(loginResponse.getErrorMessage()).orElse("")));
        }
    }

    private void configureDeviceIfNotConfigured() {
        if (Strings.isNullOrEmpty(lclPersistentStorage.getDeviceId())) {
            String deviceId = UUID.randomUUID().toString().replaceAll("-", "");
            lclPersistentStorage.saveDeviceId(deviceId);
            apiClient.configureDevice();
        }
    }

    private String getAgentKeyForSessionId() {
        if (!Strings.isNullOrEmpty(lclPersistentStorage.getAgentKey())) {
            return lclPersistentStorage.getAgentKey();
        }

        byte[] bytes = new byte[26];
        RANDOM.nextBytes(bytes);

        String agentKey =
                EncodingUtils.encodeHexAsString(bytes).toUpperCase()
                        + LclConstants.Authentication.IDENTIFIER_FOR_VENDOR_IN_HEX;

        lclPersistentStorage.saveAgentKey(agentKey);
        return agentKey;
    }

    private static String generateSessionId(String agentKey) {
        String timeStamp = Long.toString(System.currentTimeMillis());
        long randomSessionIdSuffix = Math.abs(RANDOM.nextLong());

        return timeStamp + agentKey + randomSessionIdSuffix;
    }

    private String getXorPinInB64(String password) {
        String htmlResponse = apiClient.getXorKey();
        int xorKey = Integer.parseInt(htmlResponse);

        return EncodingUtils.encodeAsBase64String(LclCryptoUtils.computeXorPin(password, xorKey));
    }
}
