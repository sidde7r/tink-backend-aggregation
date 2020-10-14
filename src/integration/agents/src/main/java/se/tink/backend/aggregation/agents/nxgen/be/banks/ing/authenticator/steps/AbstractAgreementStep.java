package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngComponents;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage;
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
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;

public abstract class AbstractAgreementStep extends AbstractAuthenticationStep {

    protected final IngDirectApiClient ingDirectApiClient;
    protected final IngStorage ingStorage;
    protected final IngCryptoUtils ingCryptoUtils;
    private final IngRequestFactory ingRequestFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AbstractAgreementStep(String stepId, IngComponents ingComponents) {
        super(stepId);
        this.ingDirectApiClient = ingComponents.getIngDirectApiClient();
        this.ingStorage = ingComponents.getIngStorage();
        this.ingCryptoUtils = ingComponents.getIngCryptoUtils();
        this.ingRequestFactory = ingComponents.getIngRequestFactory();
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        pinIt();
        return AuthenticationStepResponse.executeNextStep();
    }

    private void pinIt() {
        String mobileAppId = ingStorage.getPermanent(Storage.MOBILE_APP_ID);
        RemoteProfileMeansResponse profileMeans = getRemoteProfileMeans(mobileAppId);

        String serverPublicValue = profileMeans.getServerPublicValue();
        String salt = getSalt();
        String passwordHex = ingStorage.getPermanent(Storage.SRP6_PASSWORD);
        byte[] password = EncodingUtils.decodeHexString(passwordHex);
        SRP6ClientValues srp6ClientValues =
                ingCryptoUtils.generateSRP6ClientValues(
                        serverPublicValue, salt, mobileAppId, password);

        String clientEvidenceMessageSignature =
                ingCryptoUtils.getClientEvidenceSignature(
                        srp6ClientValues.getEvidence().toByteArray(),
                        ingStorage.getEnrollPinningPrivateKey());

        RemoteEvidenceSessionRequest request =
                ingRequestFactory.createRemoteEvidenceSessionRequest(
                        srp6ClientValues,
                        clientEvidenceMessageSignature,
                        getRequiredLevelOfAssurance());

        IngMiscUtils.sleep(1000);

        RemoteEvidenceSessionResponse evidence =
                ingDirectApiClient.createEvidence(profileMeans.getSid(), request);

        byte[] secret = Hash.sha256(srp6ClientValues.getSecretBytes());
        generateAndStoreDerivedKeys(secret, mobileAppId, profileMeans.getHmacSalt());

        AuthenticateTokenResultEntity tokens = decryptAndMapExtra(evidence.getExtra());

        String accessToken = tokens.findAccessToken();
        ingStorage.storeAccessToken(accessToken);
        String refreshToken = tokens.findRefreshToken();
        ingStorage.storeForSession(Storage.REFRESH_TOKEN, refreshToken);
    }

    protected void generateAndStoreDerivedKeys(byte[] secret, String mobileAppId, String hmacSalt) {
        byte[] context = mobileAppId.getBytes();
        DerivedKeyOutput derivedKeyOutput =
                ingCryptoUtils.deriveKeys(secret, EncodingUtils.decodeHexString(hmacSalt), context);

        ingStorage.storeEncryptionKey(derivedKeyOutput.getEncryptionKey());
        ingStorage.storeSigningKey(derivedKeyOutput.getSigningKey());
    }

    protected AuthenticateTokenResultEntity decryptAndMapExtra(String extra) {
        try {
            SecretKey encryptionKey = ingStorage.getEncryptionKey();
            SecretKey convertedKey = new SecretKeySpec(encryptionKey.getEncoded(), "AES");
            String decryptedExtra = ingCryptoUtils.decryptExtra(extra, convertedKey);

            return objectMapper.readValue(decryptedExtra, AuthenticateTokenResultEntity.class);
        } catch (IOException | GeneralSecurityException ex) {
            throw new IllegalStateException("Could not deserialize or decrypt extra", ex);
        }
    }

    protected abstract RemoteProfileMeansResponse getRemoteProfileMeans(String mobileAppId);

    protected abstract String getSalt();

    protected abstract int getRequiredLevelOfAssurance();
}
