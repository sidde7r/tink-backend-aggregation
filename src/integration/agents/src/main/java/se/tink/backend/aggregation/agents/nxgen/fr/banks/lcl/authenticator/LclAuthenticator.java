package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclConstants.Authentication.INCORRECT_CREDENTIALS_CODE_LIST;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
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
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.libraries.encoding.EncodingUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LclAuthenticator implements PasswordAuthenticator {

    private final LclApiClient apiClient;

    private final LclPersistentStorage lclPersistentStorage;

    private final RandomValueGenerator randomValueGenerator;

    private final LocalDateTimeSource localDateTimeSource;

    public LclAuthenticator(
            LclApiClient apiClient,
            LclPersistentStorage lclPersistentStorage,
            RandomValueGenerator randomValueGenerator,
            LocalDateTimeSource localDateTimeSource) {
        this.apiClient = apiClient;
        this.lclPersistentStorage = lclPersistentStorage;
        this.randomValueGenerator = randomValueGenerator;
        this.localDateTimeSource = localDateTimeSource;
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

            if (INCORRECT_CREDENTIALS_CODE_LIST.contains(errorCode)) {
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
            String deviceId = randomValueGenerator.getUUID().toString();
            lclPersistentStorage.saveDeviceId(deviceId);
            apiClient.configureDevice();
        }
    }

    private String getAgentKeyForSessionId() {
        if (!Strings.isNullOrEmpty(lclPersistentStorage.getAgentKey())) {
            return lclPersistentStorage.getAgentKey();
        }

        byte[] bytes = randomValueGenerator.secureRandom(26);

        String agentKey =
                EncodingUtils.encodeHexAsString(bytes).toUpperCase()
                        + LclConstants.Authentication.IDENTIFIER_FOR_VENDOR_IN_HEX;

        lclPersistentStorage.saveAgentKey(agentKey);
        return agentKey;
    }

    private String generateSessionId(String agentKey) {
        String timeStamp = Long.toString(localDateTimeSource.getInstant().toEpochMilli());
        BigInteger sessionIdSuffix = new BigInteger(1, randomValueGenerator.secureRandom(64));
        return String.format("MP%s%s%s", timeStamp, agentKey, sessionIdSuffix);
    }

    private String getXorPinInB64(String password) {
        String htmlResponse = apiClient.getXorKey();
        int xorKey = Integer.parseInt(htmlResponse);

        return EncodingUtils.encodeAsBase64String(LclCryptoUtils.computeXorPin(password, xorKey));
    }
}
