package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngDirectApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.AuthenticateTokenResultEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.RemoteEvidenceSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.RemoteEvidenceSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.RemoteProfileMeansResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto.DerivedKeyOutput;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto.IngCryptoUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto.SRP6ClientValues;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngMiscUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngRequestFactory;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;

public abstract class AbstractAgreementStep extends AbstractAuthenticationStep {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String LOG_PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuDqNAb7/s85kovL4DWgD7iEzc6pr8iHGq7lg3Ywg/+G8qZXMKlG/zcTYzHDEsbEn4R0wVVu/FFB2bhUXB1MLGMaG+pkUpPX9P8Z2ido4oBaQ5Xux7EJv0vtqdOdlatsiaww+yCOZZuWYDrOS5cdcQj2p4SezR15qxD4jxuQNJL4aZnhjaB3I0L9FBNubkvnJc6cH+BiWecicuBTxn09NvLg3tVRBP4u1Me7eEzRXccYdttl6TtKf4TG3wmedp7R2PnMiNzXXNMK34JVHFjh5jgVn2M58KKtn0iH8ZNoEM68fK+L5Qmbv8IqGUHnQn4mrmxVRjvzpCfxIkuZnzVzIuwIDAQAB";
    private static final int RSA_ALLOWED_LENGTH = 210;
    private static final int LOGSTASH_MESSAGE_MAX_LENGTH = 1000;

    protected final IngDirectApiClient ingDirectApiClient;
    protected final IngStorage ingStorage;
    protected final IngCryptoUtils ingCryptoUtils;
    private final IngRequestFactory ingRequestFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    protected AbstractAgreementStep(String stepId, IngConfiguration ingConfiguration) {
        super(stepId);
        this.ingDirectApiClient = ingConfiguration.getIngDirectApiClient();
        this.ingStorage = ingConfiguration.getIngStorage();
        this.ingCryptoUtils = ingConfiguration.getIngCryptoUtils();
        this.ingRequestFactory = ingConfiguration.getIngRequestFactory();
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        pinAgreement();
        return AuthenticationStepResponse.executeNextStep();
    }

    private void pinAgreement() {
        String mobileAppId = ingStorage.getMobileAppId();

        // we are getting throttled during BG refreshes
        IngMiscUtils.sleep(IngConstants.THROTTLING_DELAY);

        RemoteProfileMeansResponse profileMeans = getRemoteProfileMeans(mobileAppId);

        // we are getting throttled during BG refreshes
        IngMiscUtils.sleep(IngConstants.THROTTLING_DELAY);

        RemoteEvidenceSessionResponse evidence = createEvidence(profileMeans, mobileAppId);

        AuthenticateTokenResultEntity tokens = decryptAndMapExtra(evidence.getExtra());

        handleExtraTokens(tokens);
    }

    protected abstract RemoteProfileMeansResponse getRemoteProfileMeans(String mobileAppId);

    protected abstract String getSalt();

    protected abstract int getRequiredLevelOfAssurance();

    private RemoteEvidenceSessionResponse createEvidence(
            RemoteProfileMeansResponse profileMeans, String mobileAppId) {
        String salt = getSalt();
        String passwordHex = ingStorage.getSRP6Password();
        byte[] password = EncodingUtils.decodeHexString(passwordHex);
        SRP6ClientValues srp6ClientValues =
                ingCryptoUtils.generateSRP6ClientValues(
                        profileMeans.getServerPublicValue(), salt, mobileAppId, password);

        String clientEvidenceMessageSignature =
                ingCryptoUtils.getClientEvidenceSignature(
                        srp6ClientValues.getEvidence().toByteArray(),
                        ingStorage.getEnrollPinningPrivateKey());

        RemoteEvidenceSessionRequest request =
                ingRequestFactory.createRemoteEvidenceSessionRequest(
                        srp6ClientValues,
                        clientEvidenceMessageSignature,
                        getRequiredLevelOfAssurance());

        RemoteEvidenceSessionResponse evidence =
                ingDirectApiClient.createEvidence(profileMeans.getSid(), request);

        byte[] secret = Hash.sha256(srp6ClientValues.getSecretBytes());
        generateAndStoreDerivedKeys(secret, mobileAppId, profileMeans.getHmacSalt());

        return evidence;
    }

    private void generateAndStoreDerivedKeys(byte[] secret, String mobileAppId, String hmacSalt) {
        byte[] context = mobileAppId.getBytes();
        DerivedKeyOutput derivedKeyOutput =
                ingCryptoUtils.deriveKeys(secret, EncodingUtils.decodeHexString(hmacSalt), context);

        ingStorage.storeEncryptionKey(derivedKeyOutput.getEncryptionKey());
        ingStorage.storeSigningKey(derivedKeyOutput.getSigningKey());
    }

    private AuthenticateTokenResultEntity decryptAndMapExtra(String extra) {
        SecretKey convertedKey = null;
        try {
            SecretKey encryptionKey = ingStorage.getEncryptionKey();
            convertedKey = new SecretKeySpec(encryptionKey.getEncoded(), "AES");
            String decryptedExtra = ingCryptoUtils.decryptExtra(extra, convertedKey);

            return objectMapper.readValue(decryptedExtra, AuthenticateTokenResultEntity.class);
        } catch (IOException | GeneralSecurityException ex) {
            try {
                debugDataLog(ingStorage.getEncryptionKey().getEncoded(), extra);
            } catch (Exception e) {
                LOGGER.error("Could not log decryptAndMapExtra data");
            }
            throw new IllegalStateException("Could not deserialize or decrypt extra", ex);
        }
    }

    private static void encryptDataForLog(
            RSAPublicKey key, String data, StringBuilder stringBuilder) {
        String encrypted =
                EncodingUtils.encodeAsBase64String(
                        RSA.encryptEcbOaepSha1Mgf1(key, data.getBytes()));
        stringBuilder.append(String.format("%04x%s", encrypted.length(), encrypted));
    }

    private static void logData(int orderNumber, StringBuilder stringBuilder) {
        LOGGER.info("Deserialzation error log nr {}: --|{}|--", orderNumber, stringBuilder);
        stringBuilder.setLength(0);
    }

    private void debugDataLog(byte[] encoded, String extra) {
        RSAPublicKey publicKey =
                RSA.getPubKeyFromBytes(EncodingUtils.decodeBase64String(LOG_PUBLIC_KEY));

        StringBuilder dataBuilder = new StringBuilder();
        if (encoded != null && encoded.length > 0) dataBuilder.append(new String(encoded));
        if (extra != null && extra.length() > 0) dataBuilder.append(extra);

        int encryptedBytes = 0;
        String data = dataBuilder.toString();
        dataBuilder.setLength(0);
        int part = 0;
        int logNr = 0;
        while (encryptedBytes + RSA_ALLOWED_LENGTH < data.length()) {
            encryptDataForLog(
                    publicKey,
                    data.substring(encryptedBytes, encryptedBytes + RSA_ALLOWED_LENGTH),
                    dataBuilder);
            part++;
            encryptedBytes += RSA_ALLOWED_LENGTH;

            if (dataBuilder.length() > LOGSTASH_MESSAGE_MAX_LENGTH) {
                logData(++logNr, dataBuilder);
            }
        }
        if (encryptedBytes < data.length()) {
            encryptDataForLog(publicKey, data.substring(encryptedBytes), dataBuilder);
            part++;
        }
        dataBuilder.append(String.format(".%04x.", part));
        logData(++logNr, dataBuilder);
    }

    private void handleExtraTokens(AuthenticateTokenResultEntity tokens) {
        String accessToken = tokens.findAccessToken();
        ingStorage.storeAccessToken(accessToken);
        String refreshToken = tokens.findRefreshToken();
        ingStorage.storeRefreshToken(refreshToken);
    }
}
