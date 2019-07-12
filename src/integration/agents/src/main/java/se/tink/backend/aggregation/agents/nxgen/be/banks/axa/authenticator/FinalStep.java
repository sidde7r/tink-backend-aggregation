package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator;

import java.util.Collections;
import java.util.UUID;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.RegisterUserResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.StoreRegistrationCdResponse;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.Digipass;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.models.DecryptActivationDataResponse;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.models.InitializeRegistrationDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;

final class FinalStep implements AuthenticationStep {

    private final AxaApiClient apiClient;
    private final AxaStorage storage;

    FinalStep(final AxaApiClient apiClient, final AxaStorage storage) {
        this.apiClient = apiClient;
        this.storage = storage;
    }

    @Override
    public AuthenticationResponse respond(final AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        final Credentials credentials = request.getCredentials();

        final String creditCardNo = credentials.getField(Field.Key.USERNAME);
        final String activationPassword =
                storage.getActivationPassword().orElseThrow(IllegalStateException::new);
        final String challenge = storage.getChallenge().orElseThrow(IllegalStateException::new);
        final Digipass cryptoModule = new Digipass();
        final InitializeRegistrationDataResponse digipassResponse1 =
                cryptoModule.initializeRegistrationData(activationPassword);
        final String basicAuth = AxaConstants.Request.BASIC_AUTH;

        storage.persistBasicAuth(basicAuth);
        storage.persistClientInitialVectorInit(digipassResponse1.getClientInitialVector());
        storage.persistEncryptedPublicKeyAndNonce(
                digipassResponse1.getEncryptedClientPublicKeyAndNonce());
        storage.persistDigipass(cryptoModule);

        final String encryptedClientPublicKeyAndNonce =
                digipassResponse1.getEncryptedClientPublicKeyAndNonce();
        final String clientInitialVector1 = digipassResponse1.getClientInitialVector();
        final UUID deviceId = UUID.randomUUID();
        final String ucrid = storage.getUcrid().orElseThrow(IllegalStateException::new);
        final String responseCode =
                request.getUserInputs().stream()
                        .filter(input -> !input.contains(" "))
                        .findAny()
                        .orElseThrow(IllegalStateException::new);

        storage.persistDeviceId(deviceId);

        final RegisterUserResponse registerResponse =
                apiClient.postRegisterUser(
                        basicAuth,
                        ucrid,
                        deviceId,
                        creditCardNo,
                        challenge,
                        responseCode,
                        clientInitialVector1.toUpperCase(),
                        encryptedClientPublicKeyAndNonce.toUpperCase());

        if (registerResponse.isIncorrectResponseError()) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
        } else if (registerResponse.isRegistrationLimitReachedError()) {
            throw LoginError.REGISTER_DEVICE_ERROR.exception();
        } else if (registerResponse.isIncorrectCardNumberError()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else if (registerResponse.isUnrecognizedBankUser()) {
            throw LoginError.NOT_CUSTOMER.exception();
        }

        storage.persistServerInitialVector(registerResponse.getServerInitialVector());
        storage.persistEncryptedNonces(registerResponse.getEncryptedNonces());
        storage.persistEncryptedServerPublicKey(registerResponse.getEncryptedServerPublicKey());
        storage.persistXfad(registerResponse.getXfad());
        storage.persistRegisterChallenge(registerResponse.getRegisterChallenge());
        storage.persistSerialNo(registerResponse.getSerialNo());

        final String serverInitialVector = registerResponse.getServerInitialVector();
        final String encryptedNonces = registerResponse.getEncryptedNonces();
        final String encryptedServerPublicKey = registerResponse.getEncryptedServerPublicKey();
        final String xfad = registerResponse.getXfad();
        final String registerChallenge = registerResponse.getRegisterChallenge();
        final String serialNo = registerResponse.getSerialNo();

        final DecryptActivationDataResponse digipassResponse2 =
                cryptoModule.decryptActivationData(
                        serverInitialVector,
                        encryptedNonces,
                        encryptedServerPublicKey,
                        xfad,
                        registerChallenge);

        storage.persistDigipass(cryptoModule);
        storage.persistClientInitialVectorDecrypt(digipassResponse2.getClientInitialVector());
        storage.persistEncryptedServerNonce(digipassResponse2.getEncryptedServerNonce());
        storage.persistDerivationCode(digipassResponse2.getDerivationCode());

        final String clientInitialVector2 = digipassResponse2.getClientInitialVector();
        final String derivationCode = digipassResponse2.getDerivationCode();
        final String encryptedServerNonce = digipassResponse2.getEncryptedServerNonce();

        final StoreRegistrationCdResponse storeDerivationCdResponse =
                apiClient.postStoreDerivation(
                        basicAuth,
                        clientInitialVector2,
                        derivationCode,
                        encryptedServerNonce,
                        serialNo);

        storage.persistStoreRegistrationCdResponse(storeDerivationCdResponse);

        AxaCommonAuthenticator.authenticate(apiClient, storage);

        return new AuthenticationResponse(Collections.emptyList());
    }
}
