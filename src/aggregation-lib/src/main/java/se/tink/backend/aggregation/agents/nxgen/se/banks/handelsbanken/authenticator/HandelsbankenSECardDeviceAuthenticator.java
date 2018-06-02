package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator;

import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.device.CheckAgreementResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.validators.CheckAgreementResponseValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
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
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

public class HandelsbankenSECardDeviceAuthenticator implements MultiFactorAuthenticator {

    private final HandelsbankenSEApiClient client;
    private final HandelsbankenPersistentStorage persistentStorage;
    private final SupplementalInformationController supplementalInformationController;
    private final HandelsbankenConfiguration configuration;

    public HandelsbankenSECardDeviceAuthenticator(HandelsbankenSEApiClient client,
            HandelsbankenPersistentStorage persistentStorage,
            SupplementalInformationController supplementalInformationController,
            HandelsbankenConfiguration configuration) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.supplementalInformationController = supplementalInformationController;
        this.configuration = configuration;
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

        Map<String, String> supplementalInformation = supplementalInformationController
                .askSupplementalInformation(challengeField(initNewProfile), responseField());

        String code = supplementalInformation.get(HandelsbankenConstants.DeviceAuthentication.CODE);
        if (code.length() == 8) {
            // MasterCard instead of login card has been used in the card reader
            HandelsbankenConstants.DeviceAuthentication.OtherUserError.WRONG_CARD.throwException();
        }

        CreateProfileResponse createProfile = client.createProfile(
                initNewProfile,
                EncryptedUserCredentialsRequest.create(
                        initNewProfile,
                        UserCredentialsRequest.create(credentials.getField(Field.Key.USERNAME), code),
                        tfa)
        );

        ActivateProfileResponse activateProfile = client.activateProfile(
                createProfile,
                ActivateProfileRequest.create(createProfile, tfa)
        );

        CommitProfileResponse commitProfile = client.commitProfile(activateProfile);

        new CommitProfileResponseValidator(commitProfile).validate();

        CheckAgreementResponse checkAgreement = client.checkAgreement(commitProfile);

        new CheckAgreementResponseValidator(checkAgreement).validate();

        persistentStorage.persist(activateProfile);
        persistentStorage.persist(tfa);
    }

    private Field challengeField(InitNewProfileResponse initNewProfile) {
        Field field = new Field();
        field.setImmutable(true);
        field.setDescription("Kontrollkod");
        field.setValue(initNewProfile.getChallenge());
        field.setName("challenge");
        field.setHelpText("Sätt i ditt inloggningskort i kortläsaren och tryck på knappen SIGN. "
                + "Knappa därefter in kontrollkoden och din PIN-kod till kortet i kortläsaren. "
                + "Skriv in svarskoden i fältet nedan.");
        return field;
    }

    private Field responseField() {
        Field field = new Field();
        field.setDescription("Svarskod");
        field.setName(HandelsbankenConstants.DeviceAuthentication.CODE);
        field.setNumeric(true);
        field.setHint("NNN NNN NNN");
        field.setMaxLength(9);
        field.setMinLength(8); // Allow 8 digits, to be able to return a helpful error message for that case.
        field.setPattern("([0-9]{8,9})");
        return field;
    }
}
