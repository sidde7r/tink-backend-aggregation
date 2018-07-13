package se.tink.backend.aggregation.agents.nxgen.demo.banks.multisupplemental.authenticator;

import com.google.common.base.Preconditions;
import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

public class MultiSupplementalManualAuthenticator implements MultiFactorAuthenticator {
    private static final String FIELD0 = "field0";
    private static final String FIELD1 = "field1";
    private static final String FIELD2 = "field2";
    private static final String FIELD3 = "field3";
    private static final String FIELD4 = "field4";
    private static final String FIELD5 = "field5";

    private final SupplementalInformationController supplementalInformationController;

    public MultiSupplementalManualAuthenticator(SupplementalInformationController supplementalInformationController) {
        this.supplementalInformationController = supplementalInformationController;
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

    private static void checkAnswers(Map<String, String> answer, String... fieldNames) {
        Preconditions.checkNotNull(answer);
        for (String fieldName : fieldNames) {
            Preconditions.checkState(answer.containsKey(fieldName), "Did not contain %s", fieldName);
        }
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        checkAnswers(
                supplementalInformationController.askSupplementalInformation(
                        newField(FIELD0, "Test field 0", null),
                        newField(FIELD1, "Test field 1", "test1-value")),
                FIELD0,
                FIELD1
        );

        checkAnswers(
                supplementalInformationController.askSupplementalInformation(
                        newField(FIELD2, "Test field 2", "test2-value"),
                        newField(FIELD3, "Test field 3", null)),
                FIELD2,
                FIELD3
        );

        checkAnswers(
                supplementalInformationController.askSupplementalInformation(
                        newField(FIELD4, "Test field 4", null),
                        newField(FIELD5, "Test field 5", "test5-value")),
                FIELD4,
                FIELD5
        );
    }
}
