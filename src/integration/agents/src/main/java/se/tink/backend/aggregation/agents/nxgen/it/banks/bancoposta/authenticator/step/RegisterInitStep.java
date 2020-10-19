package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import java.util.UUID;
import lombok.SneakyThrows;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.FormParams;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.RegisterInitBody;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.AccessTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.RegisterInitResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.RegisterResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt.RegisterInitJWEManager;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.libraries.retrypolicy.RetryExecutor;
import se.tink.libraries.retrypolicy.RetryPolicy;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class RegisterInitStep implements AuthenticationStep {
    private static final int RETRY_ATTEMPTS = 3;

    private final BancoPostaApiClient apiClient;
    private final BancoPostaStorage storage;
    private final RegisterInitJWEManager jweManager;
    private final RetryExecutor retryExecutor = new RetryExecutor();

    public RegisterInitStep(BancoPostaApiClient apiClient, BancoPostaStorage storage) {
        this.apiClient = apiClient;
        this.storage = storage;
        this.jweManager = new RegisterInitJWEManager(storage);
        this.retryExecutor.setRetryPolicy(new RetryPolicy(RETRY_ATTEMPTS, LoginException.class));
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        String initCodeChallengeInput = UUID.randomUUID().toString();
        String keyPairSerialized = SerializationUtils.serializeKeyPair(RSA.generateKeyPair());
        storage.saveToPersistentStorage(Storage.KEY_PAIR, keyPairSerialized);

        registerInit(initCodeChallengeInput);

        register(initCodeChallengeInput);

        activate();

        // Sometimes even when user entered correct credentials bank throw errors login_required and
        // next call with the same values get through. Hence retry executor used
        AccessTokenResponse accessTokenResponse =
                retryExecutor.execute(() -> requestForAccessToken(request.getCredentials()));
        storage.saveToPersistentStorage(
                Storage.REGISTRATION_SESSION_TOKEN, accessTokenResponse.getAccessToken());

        return AuthenticationStepResponse.executeStepWithId(
                BancoPostaAuthenticator.REGSITER_VERIFICATION_STEP_ID);
    }

    private void activate() {
        String activationJWE = jweManager.genActivationJWE();
        apiClient.activate(activationJWE);
    }

    private AccessTokenResponse requestForAccessToken(Credentials credentials) {
        String username = credentials.getField(Key.USERNAME);
        String password = credentials.getField(Key.PASSWORD);
        String jweObject = jweManager.genAZTokenJWE(username, password);

        Form azForm = buildAZRequestForm();
        String requestToken = apiClient.performRequestAz(azForm.serialize());

        Form formCredentials = buildAZForm(jweObject, requestToken);
        return apiClient.performOpenIdAz(formCredentials);
    }

    private void registerInit(String initCodeChallengeInput) {
        byte[] challengeByte = Hash.sha256(initCodeChallengeInput);
        String initCodeChallenge = EncodingUtils.encodeAsBase64String(challengeByte);

        RegisterInitBody registerInitBody =
                new RegisterInitBody(initCodeChallenge, FormValues.APP_NAME);
        RegisterInitResponse registerInitResponse = apiClient.registerInit(registerInitBody);

        storage.saveToPersistentStorage(
                Storage.PUB_SERVER_KEY, registerInitResponse.getPubServerKey());
    }

    @SneakyThrows
    private void register(String initCodeChallengeInput) {
        String jwe = jweManager.genRegisterJWE(initCodeChallengeInput);

        String registerResponseJWE = apiClient.register(jwe);

        JWEObject jweObject = JWEObject.parse(registerResponseJWE);
        jweObject.decrypt(new RSADecrypter(storage.getKeyPair().getPrivate()));
        String json = jweObject.getPayload().toString();
        RegisterResponse registerResponse =
                SerializationUtils.deserializeFromString(json, RegisterResponse.class);

        String otpSecretKey = registerResponse.getData().getOtpSecretKey();
        storage.saveToPersistentStorage(Storage.OTP_SECRET_KEY, otpSecretKey);
        storage.saveToPersistentStorage(Storage.APP_ID, registerResponse.getData().getAppUuid());
    }

    private Form buildAZRequestForm() {
        return Form.builder()
                .put(FormParams.SCOPE, FormValues.SCOPE)
                .put(FormParams.APP_NAME, FormValues.APP_NAME)
                .put(FormParams.ACR_VALUES, FormValues.ACR_VALUES)
                .put(FormParams.SUB, FormValues.SUB)
                .put(FormParams.GRANT_TYPE, FormValues.GRANT_TYPE)
                .build();
    }

    private Form buildAZForm(String jweObject, String requestToken) {
        return Form.builder()
                .put(FormParams.REQUEST, requestToken)
                .put(FormParams.CREDENTIALS, jweObject)
                .put(FormParams.RESPONSE_TYPE, FormValues.TOKEN)
                .build();
    }
}
