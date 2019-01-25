package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator;

import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.entities.SecurityCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.rpc.device.EncryptedSecurityCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.rpc.device.SecurityCardResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.rpc.device.VerifySecurityCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.validators.ActivateProfileValidator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.validators.SecurityCardResponseValidator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.validators.VerifySecurityCodeResponseValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.HandelsbankenAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.encryption.LibTFA;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.EntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.UserCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.ActivateProfileRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.ActivateProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.CommitProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.CreateProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.EncryptedUserCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.InitNewProfileRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.InitNewProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators.CommitProfileResponseValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

public class HandelsbankenFICardDeviceAuthenticator implements MultiFactorAuthenticator {

    private final HandelsbankenFIApiClient client;
    private final HandelsbankenPersistentStorage persistentStorage;
    private final SupplementalInformationController supplementalInformationController;
    private final HandelsbankenConfiguration configuration;
    private final HandelsbankenAutoAuthenticator autoAuthenticator;

    public HandelsbankenFICardDeviceAuthenticator(HandelsbankenFIApiClient client,
            HandelsbankenPersistentStorage persistentStorage,
            SupplementalInformationController supplementalInformationController,
            HandelsbankenConfiguration configuration,
            HandelsbankenAutoAuthenticator autoAuthenticator) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.supplementalInformationController = supplementalInformationController;
        this.configuration = configuration;
        this.autoAuthenticator = autoAuthenticator;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        LibTFA tfa = new LibTFA();

        EntryPointResponse entryPoint = client.fetchEntryPoint();

        InitNewProfileResponse initNewProfile = client.initNewProfile(
                entryPoint,
                InitNewProfileRequest.create(configuration, tfa)
        );

        SecurityCardResponse securityCard = client.authenticate(
                initNewProfile,
                EncryptedUserCredentialsRequest.create(
                        initNewProfile,
                        UserCredentialsRequest.create(
                                credentials.getField(Field.Key.USERNAME),
                                credentials.getField(HandelsbankenFIConstants.DeviceAuthentication.SIGNUP_PASSWORD)
                        ),
                        tfa)
        );

        new SecurityCardResponseValidator(securityCard).validate();

        Map<String, String> securityCardCode = supplementalInformationController.askSupplementalInformation(
                challengeField(securityCard), responseField()
        );

        VerifySecurityCodeResponse verifySecurityCode = client.verifySecurityCode(
                securityCard,
                EncryptedSecurityCodeRequest.create(
                        SecurityCodeRequest.create(securityCardCode.get(HandelsbankenConstants.DeviceAuthentication.CODE)), tfa)
        );

        new VerifySecurityCodeResponseValidator(verifySecurityCode).validate();

        CreateProfileResponse createProfile = client.createProfile(verifySecurityCode);

        ActivateProfileResponse activateProfile = client.activateProfile(
                createProfile,
                ActivateProfileRequest.create(createProfile, tfa)
        );

        new ActivateProfileValidator(activateProfile).validate();

        CommitProfileResponse commitProfile = client.commitProfile(activateProfile);

        new CommitProfileResponseValidator(commitProfile).validate();

        persistentStorage.persist(activateProfile);
        persistentStorage.persist(tfa);

        // For handelsbanken FI, the carddeviceAuthenticator only unlock the device, we have to do the auto auth here
        autoAuthenticator.autoAuthenticate();
    }

    private Field challengeField(SecurityCardResponse authenticate) {
        Field field = new Field();
        field.setImmutable(true);
        field.setDescription("Challenge code");
        field.setValue(authenticate.getSecurityKeyIndex());
        field.setName("challenge");
        field.setHelpText(String.format("Find the code in your codesheet (%s) for the indicated key.",
                authenticate.getSecurityKeyCardId()
        ));
        return field;
    }

    private Field responseField() {
        Field field = new Field();
        field.setDescription("Response code");
        field.setName(HandelsbankenConstants.DeviceAuthentication.CODE);
        field.setNumeric(true);
        field.setHint("NNN NNN");
        field.setMaxLength(6);
        field.setMinLength(6);
        return field;
    }
}
