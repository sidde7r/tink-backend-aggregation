package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.libraries.i18n.Catalog;

public class KeyCardAuthenticationController implements MultiFactorAuthenticator {
    private static final int DEFAULT_KEY_CARD_VALUE_LENGTH = 6;

    private final Catalog catalog;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final KeyCardAuthenticator authenticator;
    private final int keyCardValueLength;

    private static final String KEYCARD_VALUE_FIELD_KEY = "keyCardValue";

    public KeyCardAuthenticationController(Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper, KeyCardAuthenticator authenticator) {
        this(catalog, supplementalInformationHelper, authenticator, DEFAULT_KEY_CARD_VALUE_LENGTH);
    }

    public KeyCardAuthenticationController(Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper,
            KeyCardAuthenticator authenticator, int keyCardValueLength) {
        this.catalog = Preconditions.checkNotNull(catalog);
        this.supplementalInformationHelper = Preconditions.checkNotNull(supplementalInformationHelper);
        this.authenticator = Preconditions.checkNotNull(authenticator);
        this.keyCardValueLength = keyCardValueLength;
    }

    @Override
    public CredentialsTypes getType() {
        // TODO: Change to a multifactor type when supported
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        NotImplementedException.throwIf(!Objects.equals(credentials.getType(), getType()),
                String.format("Authentication method not implemented for CredentialsType: %s", credentials.getType()));

        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        KeyCardInitValues keyCardInitValues = authenticator.init(username, password);

        Map<String, String> supplementalInformation = supplementalInformationHelper
                .askSupplementalInformation(getKeyCardIndexField(keyCardInitValues), getKeyCardValueField());

        authenticator.authenticate(supplementalInformation.get(KEYCARD_VALUE_FIELD_KEY));
    }

    private Field getKeyCardIndexField(KeyCardInitValues keyCardInitValues) {
        Optional<String> keyCardId = keyCardInitValues.getCardId();
        String keyCardCodeIndex = keyCardInitValues.getCardIndex();

        String helpText = catalog.getString("Input the code from your code card");
        if (keyCardId.isPresent()) {
            helpText = helpText + String.format(" (%s)", keyCardId.get());
        }

        Field keyIndexField = new Field();
        keyIndexField.setDescription(catalog.getString("Key card index"));
        keyIndexField.setName("keyCardIndex");
        keyIndexField.setHelpText(helpText);
        keyIndexField.setValue(keyCardCodeIndex);
        keyIndexField.setImmutable(true);
        return keyIndexField;
    }

    private Field getKeyCardValueField() {
        Field codeCardValue = new Field();
        codeCardValue.setDescription(catalog.getString("Key card code"));
        codeCardValue.setName(KEYCARD_VALUE_FIELD_KEY);
        codeCardValue.setNumeric(true);
        codeCardValue.setMinLength(keyCardValueLength);
        codeCardValue.setMaxLength(keyCardValueLength);
        codeCardValue.setHint(StringUtils.repeat("N", keyCardValueLength));
        codeCardValue.setPattern(String.format("([0-9]{%d})", keyCardValueLength));
        codeCardValue.setPatternError("The code you entered is not valid");

        return codeCardValue;
    }
}
