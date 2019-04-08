package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator;

import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.GenerateChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.RegisterUserResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.StoreRegistrationCdResponse;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.Digipass;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.models.DecryptActivationDataResponse;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.models.InitializeRegistrationDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.i18n.Catalog;

public final class AxaManualAuthenticator implements MultiFactorAuthenticator {

    private final AxaApiClient apiClient;
    private final Catalog catalog;
    private final AxaStorage storage;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public AxaManualAuthenticator(
            final Catalog catalog,
            final AxaApiClient apiClient,
            final AxaStorage storage,
            final SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.catalog = catalog;
        this.storage = storage;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    private static String generateUcrid() {
        return RandomStringUtils.randomNumeric(32);
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        // First request
        final String basicAuth = AxaConstants.Request.BASIC_AUTH;
        final String ucrid = generateUcrid();
        final GenerateChallengeResponse challengeResponse =
                apiClient.postGenerateChallenge(basicAuth, ucrid);

        // Request supplemental info from card reader
        final String responseCode = waitForLoginCode(challengeResponse.getChallenge());

        // Second request
        final String creditCardNo = credentials.getField(Field.Key.USERNAME);
        final String activationPassword = challengeResponse.getActivationPassword();
        final String challenge = challengeResponse.getChallenge();
        final Digipass cryptoModule = new Digipass();
        final InitializeRegistrationDataResponse digipassResponse1 =
                cryptoModule.initializeRegistrationData(activationPassword);

        storage.persistBasicAuth(basicAuth);
        storage.persistClientInitialVectorInit(digipassResponse1.getClientInitialVector());
        storage.persistEncryptedPublicKeyAndNonce(
                digipassResponse1.getEncryptedClientPublicKeyAndNonce());
        storage.persistDigipass(cryptoModule);

        final String encryptedClientPublicKeyAndNonce =
                digipassResponse1.getEncryptedClientPublicKeyAndNonce();
        final String clientInitialVector1 = digipassResponse1.getClientInitialVector();
        final UUID deviceId = UUID.randomUUID();

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

        // Third request
        final StoreRegistrationCdResponse storeDerivationCdResponse =
                apiClient.postStoreDerivation(
                        basicAuth,
                        clientInitialVector2,
                        derivationCode,
                        encryptedServerNonce,
                        serialNo);

        storage.persistStoreRegistrationCdResponse(storeDerivationCdResponse);

        AxaCommonAuthenticator.authenticate(apiClient, storage);
    }

    private String waitForLoginCode(final String challengeCode) throws SupplementalInfoException {
        return supplementalInformationHelper.waitForLoginChallengeResponse(challengeCode);
    }
}
