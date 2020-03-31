package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice;

import java.util.Map;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice.exception.ClientAnswerException;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class TanAnswerProvider {
    private static final String GENERATED_TAN_KEY = "generatedTAN";
    private final SupplementalInformationHelper supplementalInformationHelper;

    public TanAnswerProvider(SupplementalInformationHelper supplementalInformationHelper) {
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    public String getTanAnswer() {
        Map<String, String> supplementalInformation;
        try {
            supplementalInformation =
                    supplementalInformationHelper.askSupplementalInformation(
                            getFieldForGeneratedTan());
        } catch (SupplementalInfoException e) {
            throw new ClientAnswerException("Could not get TAN Answer", e);
        }

        return supplementalInformation.get(GENERATED_TAN_KEY);
    }

    private Field getFieldForGeneratedTan() {
        return Field.builder()
                .description("Enter Generated TAN")
                .name(GENERATED_TAN_KEY)
                .numeric(false)
                .minLength(1)
                .maxLength(999)
                .build();
    }
}
