package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator;

import com.google.common.base.Strings;
import java.util.Map;
import se.tink.backend.aggregation.agents.banks.crosskey.utils.CrossKeyUtils;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.UnexpectedFailureException;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.AddDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.AddDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.ConfirmTanCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.ConfirmTanCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.LoginWithoutTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.LoginWithoutTokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

public class AlandsBankenMultifFactorAuthenticator implements MultiFactorAuthenticator {

    private final AlandsBankenApiClient client;
    private final AlandsBankenPersistentStorage persistentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public AlandsBankenMultifFactorAuthenticator(
            AlandsBankenApiClient client,
            AlandsBankenPersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        LoginWithoutTokenResponse challenge = client.loginWithoutToken(
                new LoginWithoutTokenRequest()
                        .setUsername(username)
                        .setPassword(password)
        );
        challenge.validate(() -> new UnexpectedFailureException(challenge, "Failure on login"));

        Map<String, String> supplementalInformation = supplementalInformationHelper.askSupplementalInformation(
                getKeyField(challenge), getTokenField());

        ConfirmTanCodeResponse confirmation = client.confirmTanCode(
                new ConfirmTanCodeRequest()
                        .setTan(supplementalInformation.get(
                                AlandsBankenConstants.MultiFactorAuthentication.TAN))
        );
        confirmation.validate(() -> new UnexpectedFailureException(confirmation, "Failure on confirming tan code"));

        AddDeviceResponse addDevice = client.addDevice(
                new AddDeviceRequest()
                        .setUdId(CrossKeyUtils.generateUdIdFor(credentials.getUserId()))
        );
        addDevice.validate(() -> new UnexpectedFailureException(addDevice, "Failure on adding of new device"));

        persistentStorage.persist(addDevice);
    }

    private Field getKeyField(LoginWithoutTokenResponse challenge) {
        Field keyField = new Field();
        keyField.setMasked(false);
        keyField.setDescription("Engångskod");
        keyField.setName("key");
        keyField.setHelpText("Ange koden från ditt kodhäfte, dubbelkolla så att koden du skriver in har rätt plats i kodhäftet");
        keyField.setValue(challenge.getTanPosition());
        keyField.setImmutable(true);
        return keyField;
    }

    private Field getTokenField() {
        Field tokenField = new Field();
        tokenField.setMasked(false);
        tokenField.setDescription("Engångskod");
        tokenField.setName(AlandsBankenConstants.MultiFactorAuthentication.TAN);
        tokenField.setNumeric(true);
        tokenField.setMinLength(4);
        tokenField.setMaxLength(4);
        tokenField.setHint("NNNN");
        tokenField.setPattern("([0-9]{4})");
        return tokenField;
    }
}
