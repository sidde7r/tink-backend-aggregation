package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import java.nio.ByteBuffer;
import java.security.interfaces.RSAPublicKey;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.AgentPlatformFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.fields.OtpInputField;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EasyPinProvisionResponse;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@RequiredArgsConstructor
public class EasyPinProvisionStep
        implements AgentAuthenticationProcessStep<
                AgentUserInteractionAuthenticationProcessRequest> {

    private static final int KEY_LENGTH = 16;
    private static final int REGISTRATION_CODE_LENGTH = 8;
    private static final int TIMESTAMP_LENGTH = 8;

    private final AgentPlatformFortisApiClient apiClient;
    private final FortisDataAccessorFactory dataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(
            AgentUserInteractionAuthenticationProcessRequest request) {

        FortisProcessStateAccessor processStateAccessor =
                dataAccessorFactory.createProcessStateAccessor(
                        request.getAuthenticationProcessState());

        FortisAuthDataAccessor authDataAccessor =
                dataAccessorFactory.createAuthDataAccessor(
                        request.getAuthenticationPersistedData());

        FortisProcessState processState = processStateAccessor.get();

        FortisAuthData authData = authDataAccessor.get();

        String otp = request.getUserInteractionData().getFieldValue(OtpInputField.ID);
        processState.setSmsOtp(otp);

        String registrationCode = processState.getRegistrationCode();

        String encryptionKey = RandomUtils.generateRandomHexEncoded(KEY_LENGTH);
        String signingKey = RandomUtils.generateRandomHexEncoded(KEY_LENGTH);

        byte[] message = composeMessage(encryptionKey, signingKey, registrationCode);

        RSAPublicKey publicKey =
                RSA.getPubKeyFromBytes(
                        EncodingUtils.decodeHexString(FortisConstants.PUBLIC_PROD_KEY));

        byte[] bytes = RSA.encryptNoneOaepSha1Mgf1(publicKey, message);

        String encQueryData = EncodingUtils.encodeAsBase64UrlSafe(bytes);

        EasyPinProvisionResponse response =
                apiClient.easyPinProvision(
                        encQueryData,
                        registrationCode,
                        processState.getEnrollmentSessionId(),
                        authData.getClientNumber());

        processState.setEncCredentials(response.getEncCredentials());
        processState.setEncryptionKey(encryptionKey);

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        EasyPinActivationStep.class.getSimpleName()),
                processStateAccessor.store(processState),
                request.getAuthenticationPersistedData());
    }

    private byte[] composeMessage(
            String encryptionKey, String signingKey, String registrationCode) {
        byte[] out = new byte[48];
        System.arraycopy(EncodingUtils.decodeHexString(encryptionKey), 0, out, 0, KEY_LENGTH);
        System.arraycopy(EncodingUtils.decodeHexString(signingKey), 0, out, KEY_LENGTH, KEY_LENGTH);
        System.arraycopy(
                formatRegistrationCodeV3(registrationCode),
                0,
                out,
                KEY_LENGTH * 2,
                REGISTRATION_CODE_LENGTH);
        System.arraycopy(
                getCurrentTimestampBytes(),
                0,
                out,
                KEY_LENGTH * 2 + REGISTRATION_CODE_LENGTH,
                TIMESTAMP_LENGTH);
        return out;
    }

    private byte[] formatRegistrationCodeV3(String registrationCode) {
        byte[] out = new byte[REGISTRATION_CODE_LENGTH];
        out[0] = 0x1A;
        System.arraycopy(EncodingUtils.decodeHexString(registrationCode), 0, out, 1, 5);

        // Those two bytes are randomized in strange way in the native app
        // However it was later proven that it can be anything
        out[6] = 0x01;
        out[7] = 0x02;

        return out;
    }

    private byte[] getCurrentTimestampBytes() {
        long timestamp = System.currentTimeMillis();
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(timestamp);
        return buffer.array();
    }
}
