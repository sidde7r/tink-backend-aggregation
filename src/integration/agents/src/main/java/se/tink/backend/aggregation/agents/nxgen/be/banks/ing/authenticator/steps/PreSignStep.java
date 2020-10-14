package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps;

import java.security.KeyPair;
import java.util.UUID;
import org.bouncycastle.util.encoders.Hex;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngComponents;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngProxyApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.ChallengeEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.CreateEnrollmentRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.CreateEnrollmentResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.GetSignResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto.IngCryptoUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngRequestFactory;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.CallbackProcessorMultiData;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.SupplementalFieldsAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public class PreSignStep extends SupplementalFieldsAuthenticationStep {

    private final IngProxyApiClient ingProxyApiClient;
    private final IngStorage ingStorage;
    private final IngCryptoUtils ingCryptoUtils;
    private final IngRequestFactory ingRequestFactory;

    public PreSignStep(
            IngComponents ingComponents,
            SupplementalInformationFormer supplementalInformationFormer) {
        super(
                "PRESIGN",
                callback(ingComponents.getIngStorage()),
                supplementalInformationFormer.getField(Field.Key.SIGN_CODE_DESCRIPTION),
                supplementalInformationFormer.getField(Field.Key.SIGN_CODE_INPUT));
        this.ingProxyApiClient = ingComponents.getIngProxyApiClient();
        this.ingStorage = ingComponents.getIngStorage();
        this.ingCryptoUtils = ingComponents.getIngCryptoUtils();
        this.ingRequestFactory = ingComponents.getIngRequestFactory();
    }

    private static CallbackProcessorMultiData callback(IngStorage ingStorage) {
        return callbackData -> {
            String otp = callbackData.get(Key.SIGN_CODE_INPUT.getFieldKey());
            ingStorage.storeForSession(Storage.OTP, otp);
            return AuthenticationStepResponse.executeNextStep();
        };
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        if (!alreadyPresigned()) {
            preSign();
            addValueToField();
        }
        return super.execute(request);
    }

    private void addValueToField() {
        fields.stream()
                .filter(f -> f.getName().equals(Key.SIGN_CODE_DESCRIPTION.getFieldKey()))
                .findAny()
                .ifPresent(f -> f.setValue(ingStorage.getForSession(Storage.CHALLENGE)));
    }

    private String preSign() {
        ingProxyApiClient.getIndividuals(); // response ignored

        generateAndStoreEnrollKeys();

        byte[] mpinSalt = ingCryptoUtils.getRandomBytes(16);
        byte[] deviceSalt = ingCryptoUtils.getRandomBytes(16);
        String mpinSaltStr = EncodingUtils.encodeHexAsString(mpinSalt).toUpperCase();
        String deviceSaltStr = EncodingUtils.encodeHexAsString(deviceSalt).toUpperCase();
        ingStorage.storePermanent(Storage.MPIN_SALT, mpinSaltStr);
        ingStorage.storePermanent(Storage.DEVICE_SALT, deviceSaltStr);

        String mobileAppId = UUID.randomUUID().toString();
        ingStorage.storePermanent(Storage.MOBILE_APP_ID, mobileAppId);

        byte[] randomPassword = ingCryptoUtils.getRandomBytes(160);
        ingStorage.storePermanent(Storage.SRP6_PASSWORD, Hex.toHexString(randomPassword));

        String mpinVerifier =
                ingCryptoUtils.generateSRP6Verifier(mpinSalt, mobileAppId, randomPassword);
        String deviceVerifier =
                ingCryptoUtils.generateSRP6Verifier(deviceSalt, mobileAppId, randomPassword);

        CreateEnrollmentRequestEntity requestEntity =
                ingRequestFactory.createEnrollmentRequestEntity(
                        mobileAppId, mpinVerifier, mpinSaltStr, deviceVerifier, deviceSaltStr);

        CreateEnrollmentResponseEntity enrollResponse = ingProxyApiClient.enroll(requestEntity);

        String basketId = enrollResponse.getId();
        ingStorage.storeForSession(Storage.BASKET_ID, basketId);

        GetSignResponseEntity signResponse = ingProxyApiClient.getSign(basketId);

        ChallengeEntity challengeEntity =
                signResponse.getChallenges().stream()
                        .findAny()
                        .orElseThrow(LoginError.DEFAULT_MESSAGE::exception);

        String challenge = challengeEntity.getValue();
        ingStorage.storeForSession(Storage.CHALLENGE, challenge);

        return challenge;
    }

    private boolean alreadyPresigned() {
        return ingStorage.getForSession(Storage.CHALLENGE) != null;
    }

    private void generateAndStoreEnrollKeys() {
        KeyPair keyPairPinning = ingCryptoUtils.generateKeys();
        KeyPair keyPairSigning = ingCryptoUtils.generateKeys();

        ingStorage.storeEnrollPinningPrivateKey(keyPairPinning.getPrivate());
        ingStorage.storeEnrollPinningPublicKey(keyPairPinning.getPublic());
        ingStorage.storeEnrollSigningPrivateKey(keyPairSigning.getPrivate());
        ingStorage.storeEnrollSigningPublicKey(keyPairSigning.getPublic());
    }
}
