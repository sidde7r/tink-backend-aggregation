package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator;

import java.util.Map;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.device.CheckAgreementResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.validators.CheckAgreementResponseValidator;
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
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;

public class HandelsbankenSECardDeviceAuthenticator implements TypedAuthenticator {

    private final HandelsbankenSEApiClient client;
    private final HandelsbankenPersistentStorage persistentStorage;
    private final SupplementalInformationController supplementalInformationController;
    private final HandelsbankenConfiguration configuration;
    private final HandelsbankenAutoAuthenticator autoAuthenticator;

    public HandelsbankenSECardDeviceAuthenticator(
            HandelsbankenSEApiClient client,
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
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        LibTFA tfa = new LibTFA();

        EntryPointResponse entryPoint = client.fetchEntryPoint();

        InitNewProfileResponse initNewProfile =
                client.initNewProfile(entryPoint, InitNewProfileRequest.create(configuration, tfa));

        Map<String, String> supplementalInformation =
                supplementalInformationController.askSupplementalInformationSync(
                        challengeField(initNewProfile), responseField());

        String code = supplementalInformation.get(HandelsbankenConstants.DeviceAuthentication.CODE);
        if (code.length() == 8) {
            // MasterCard instead of login card has been used in the card reader
            HandelsbankenConstants.DeviceAuthentication.OtherUserError.WRONG_CARD.throwException();
        }

        CreateProfileResponse createProfile =
                client.createProfile(
                        initNewProfile,
                        EncryptedUserCredentialsRequest.create(
                                initNewProfile,
                                UserCredentialsRequest.create(
                                        credentials.getField(Field.Key.USERNAME), code),
                                tfa));

        ActivateProfileResponse activateProfile =
                client.activateProfile(
                        createProfile, ActivateProfileRequest.create(createProfile, tfa));

        CommitProfileResponse commitProfile = client.commitProfile(activateProfile);

        new CommitProfileResponseValidator(commitProfile).validate();

        CheckAgreementResponse checkAgreement = client.checkAgreement(commitProfile);

        new CheckAgreementResponseValidator(checkAgreement).validate();

        persistentStorage.persist(activateProfile);
        persistentStorage.persist(tfa);

        autoAuthenticator.autoAuthenticate();
    }

    private Field challengeField(InitNewProfileResponse initNewProfile) {
        return Field.builder()
                .immutable(true)
                .description("Kontrollkod")
                .value(initNewProfile.getChallenge())
                .name("challenge")
                .helpText(
                        "Sätt i ditt inloggningskort i kortläsaren och tryck på knappen SIGN. "
                                + "Knappa därefter in kontrollkoden och din PIN-kod till kortet i kortläsaren. "
                                + "Skriv in svarskoden i fältet nedan.")
                .build();
    }

    private Field responseField() {
        return Field.builder()
                .description("Svarskod")
                .name(HandelsbankenConstants.DeviceAuthentication.CODE)
                .numeric(true)
                .hint("NNN NNN NNN")
                .maxLength(9)
                // Allow 8 digits, to be able to return a helpful error message for that case.
                .minLength(8)
                .pattern("([0-9]{8,9})")
                .build();
    }
}
