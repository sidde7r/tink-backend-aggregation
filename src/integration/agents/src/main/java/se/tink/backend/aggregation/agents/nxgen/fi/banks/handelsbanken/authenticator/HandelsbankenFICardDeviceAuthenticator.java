package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator;

import java.util.Map;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
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
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;

public class HandelsbankenFICardDeviceAuthenticator implements TypedAuthenticator {

    private final HandelsbankenFIApiClient client;
    private final HandelsbankenPersistentStorage persistentStorage;
    private final SupplementalInformationController supplementalInformationController;
    private final HandelsbankenConfiguration configuration;
    private final HandelsbankenAutoAuthenticator autoAuthenticator;

    public HandelsbankenFICardDeviceAuthenticator(
            HandelsbankenFIApiClient client,
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
        final String username = credentials.getField(Field.Key.USERNAME);
        credentials.setSensitivePayload(Field.Key.USERNAME, username);
        EntryPointResponse entryPoint = client.fetchEntryPoint();

        InitNewProfileResponse initNewProfile =
                client.initNewProfile(entryPoint, InitNewProfileRequest.create(configuration, tfa));

        SecurityCardResponse securityCard =
                client.authenticate(
                        initNewProfile,
                        EncryptedUserCredentialsRequest.create(
                                initNewProfile,
                                UserCredentialsRequest.create(
                                        username,
                                        credentials.getField(
                                                HandelsbankenFIConstants.DeviceAuthentication
                                                        .SIGNUP_PASSWORD)),
                                tfa));

        new SecurityCardResponseValidator(securityCard).validate();

        Map<String, String> securityCardCode =
                supplementalInformationController.askSupplementalInformationSync(
                        challengeField(securityCard), responseField());

        VerifySecurityCodeResponse verifySecurityCode =
                client.verifySecurityCode(
                        securityCard,
                        EncryptedSecurityCodeRequest.create(
                                SecurityCodeRequest.create(
                                        securityCardCode.get(
                                                HandelsbankenConstants.DeviceAuthentication.CODE)),
                                tfa));

        new VerifySecurityCodeResponseValidator(verifySecurityCode).validate();

        CreateProfileResponse createProfile = client.createProfile(verifySecurityCode);

        ActivateProfileResponse activateProfile =
                client.activateProfile(
                        createProfile, ActivateProfileRequest.create(createProfile, tfa));

        new ActivateProfileValidator(activateProfile).validate();

        CommitProfileResponse commitProfile = client.commitProfile(activateProfile);

        new CommitProfileResponseValidator(commitProfile).validate();

        persistentStorage.persist(activateProfile);
        persistentStorage.persist(tfa);

        // For handelsbanken FI, the carddeviceAuthenticator only unlock the device, we have to do
        // the auto auth here
        autoAuthenticator.autoAuthenticate();
    }

    private Field challengeField(SecurityCardResponse authenticate) {
        return Field.builder()
                .immutable(true)
                .description("Challenge code")
                .value(authenticate.getSecurityKeyIndex())
                .name("challenge")
                .helpText(
                        String.format(
                                "Find the code in your codesheet (%s) for the indicated key.",
                                authenticate.getSecurityKeyCardId()))
                .build();
    }

    private Field responseField() {
        return Field.builder()
                .description("Response code")
                .name(HandelsbankenConstants.DeviceAuthentication.CODE)
                .numeric(true)
                .hint("NNN NNN")
                .maxLength(6)
                .minLength(6)
                .build();
    }
}
