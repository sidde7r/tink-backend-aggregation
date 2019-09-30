package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.security.KeyPair;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Error;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.SupplementalFieldName;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Values;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.PrepareApprovalEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.InitScaResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.PrepareApprovalResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ErrorMessageEntity;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CommerzbankQrCodeAuthenticator implements MultiFactorAuthenticator {
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Catalog catalog;
    private final PersistentStorage persistentStorage;
    private final CommerzbankApiClient apiClient;

    public CommerzbankQrCodeAuthenticator(
            Catalog catalog,
            PersistentStorage persistentStorage,
            CommerzbankApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.catalog = catalog;
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        LoginResponse loginResponse = apiClient.manualLogin(username, password);

        if (loginResponse.getError() != null) {
            handleLoginError(loginResponse.getError());
        }

        if (!loginResponse.getLoginInfoEntity().isTanRequestedStatus()) {
            throw new IllegalStateException(
                    String.format(
                            "Excepted login status to be %s, but it was %s.",
                            Values.TAN_REQUESTED,
                            loginResponse.getLoginInfoEntity().getLoginStatus()));
        }

        scaWithQrCode();

        pinDevice();
    }

    private void scaWithQrCode() throws LoginException, SupplementalInfoException {
        InitScaResponse initScaResponse = apiClient.initScaFlow();

        if (!initScaResponse.getInitScaEntity().isPhotoTanScanningAvailable()) {
            throw LoginError.NOT_SUPPORTED.exception(
                    new LocalizableKey(
                            "We currently only support photo TAN with QR code scanning, which is not available for your user."));
        }

        String processContextId = initScaResponse.getMetaData().getProcessContextId();

        PrepareApprovalResponse prepareApprovalResponse =
                apiClient.prepareScaApproval(processContextId);

        PrepareApprovalEntity prepareApprovalEntity =
                prepareApprovalResponse.getPrepareApprovalEntity();
        String qrCodeImage = prepareApprovalEntity.getImageBase64();

        Preconditions.checkNotNull(qrCodeImage);

        Map<String, String> supplementalInformation =
                supplementalInformationHelper.askSupplementalInformation(
                        getQrCodeImageField(qrCodeImage.trim()), getPhotoTanCodeField());

        String photoTanCode = supplementalInformation.get(SupplementalFieldName.PHOTO_TAN_CODE);

        apiClient.approveSca(photoTanCode, processContextId);
        apiClient.finaliseScaApproval(processContextId);
    }

    /** Pin device and store appId and keypair which is necessary for the auto authentication. */
    private void pinDevice() {
        String appId = apiClient.initAppRegistration();
        persistentStorage.put(Storage.APP_ID, appId);

        apiClient.completeAppRegistration(appId);

        KeyPair keyPair = RSA.generateKeyPair(2048);
        persistentStorage.put(Storage.KEY_PAIR, SerializationUtils.serializeKeyPair(keyPair));

        apiClient.send2FactorToken(appId, keyPair.getPublic());
    }

    private void handleLoginError(ErrorEntity error) throws LoginException, SessionException {

        Optional<ErrorMessageEntity> errorMessage = error.getErrorMessage();

        if (!errorMessage.isPresent()) {
            throw new IllegalStateException("Login failed without error description present.");
        }

        switch (errorMessage.get().getMessageId()) {
            case Error.PIN_ERROR:
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            case Error.ACCOUNT_SESSION_ACTIVE_ERROR:
                throw SessionError.SESSION_ALREADY_ACTIVE.exception();
            default:
                throw new IllegalStateException(
                        String.format(
                                "Login failed with unknown error message: %s",
                                errorMessage.get().getMessageId()));
        }
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    private Field getQrCodeImageField(String qrCodeImage) {

        return Field.builder()
                .description(catalog.getString("Scan the QR code with the photoTAN app."))
                .name(SupplementalFieldName.BASE64_IMAGE)
                .value(qrCodeImage)
                .immutable(true)
                .build();
    }

    private Field getPhotoTanCodeField() {
        return Field.builder()
                .description(catalog.getString("TAN"))
                .name(SupplementalFieldName.PHOTO_TAN_CODE)
                .helpText(catalog.getString("Enter the TAN from the photoTAN app."))
                .numeric(true)
                .build();
    }
}
