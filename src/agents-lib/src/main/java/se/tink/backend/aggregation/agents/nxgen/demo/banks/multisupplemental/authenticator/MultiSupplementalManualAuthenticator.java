package se.tink.backend.aggregation.agents.nxgen.demo.banks.multisupplemental.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.libraries.i18n.Catalog;

public class MultiSupplementalManualAuthenticator implements MultiFactorAuthenticator {
    private static final String loginDesciptionField = "loginDescriptionField";
    private static final String loginInputField = "loginInputField";
    private static final String loginChallengeField = "loginChallengeField";
    private static final String loginChallengeInputField = "loginChallengeInputField";

    private static final String code1 = "1234";
    private static final String code2 = "4321";
    private static final String demoUsername = "tink-test";

    private final Catalog catalog;
    private static final String descriptionCode =
            "Login using your Card Reader. "
                    + "Enter the security code and press Ok. "
                    + "Provide the given return code in the input field to continue \n";

    private static final String secondDescriptionCode =
            "Login to your account by pressing 1. "
                    + "Enter the security code and press Ok. "
                    + "Provide the given return code in the input field to continue \n";

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final static Random random = new Random();

    public MultiSupplementalManualAuthenticator(SupplementalInformationHelper supplementalInformationHelper,
            Catalog catalog) {
        this.supplementalInformationHelper = supplementalInformationHelper;

        this.catalog = catalog;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    private static Field newField(String name, String description, String value) {
        Field field = new Field();
        field.setName(name);
        field.setDescription(description);
        field.setValue(value);
        return field;
    }

    private static Field newField(String name, String description, String value, String helpText) {
        Field field = newField(name, description, value);
        field.setHelpText(helpText);
        return field;
    }

    private static void checkAnswers(Map<String, String> answer, String code, String... fieldNames)
            throws LoginException {
        Preconditions.checkNotNull(answer);
        for (String fieldName : fieldNames) {
            Preconditions.checkState(answer.containsKey(fieldName), "Did not contain %s", fieldName);
            if (answer.get(fieldName).equals(code)) {
                return;
            }
        }

        throw LoginError.INCORRECT_CREDENTIALS.exception();
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        String username = credentials.getField(Field.Key.USERNAME);

        if (Strings.isNullOrEmpty(username) || !username.equals(demoUsername)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        checkAnswers(
                supplementalInformationHelper.askSupplementalInformation(
                        newField(loginDesciptionField, "Security Code" ,  String.format("%04d", random.nextInt(10000)), catalog.getString(descriptionCode)),
                        newField(loginInputField, "Input Code", null)),
                code1,
                loginDesciptionField,
                loginInputField
        );

        checkAnswers(
                supplementalInformationHelper.askSupplementalInformation(
                        newField(loginChallengeField, "Login Code", String.format("%04d", random.nextInt(10000)), catalog.getString(secondDescriptionCode)),
                        newField(loginChallengeInputField, "Input Code", null)),
                code2,
                loginChallengeField,
                loginChallengeInputField
        );
    }
}
