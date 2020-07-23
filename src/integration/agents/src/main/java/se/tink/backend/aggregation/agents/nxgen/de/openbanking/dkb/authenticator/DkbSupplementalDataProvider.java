package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class DkbSupplementalDataProvider {

    static final String GENERATED_TAN_KEY = "generatedTAN";
    private static final String GENERATED_TAN_LABEL = "Enter Generated TAN";

    static final String SELECT_AUTH_METHOD_KEY = "selectAuthMethod";
    private static final String SELECT_AUTH_METHOD_LABEL = "Authentication method index";
    private static final String SELECT_AUTH_METHOD_INFO =
            "Please insert authentication method index from 1 to %d \n";
    private static final String SELECT_AUTH_METHOD_ERROR_MESSAGE =
            "The value you entered is not valid";

    private final SupplementalInformationHelper supplementalInformationHelper;

    public DkbSupplementalDataProvider(
            SupplementalInformationHelper supplementalInformationHelper) {
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    String getTanCode() throws SupplementalInfoException {
        Map<String, String> supplementalInformation;
        supplementalInformation =
                supplementalInformationHelper.askSupplementalInformation(getFieldForGeneratedTan());
        return supplementalInformation.get(GENERATED_TAN_KEY);
    }

    private Field getFieldForGeneratedTan() {
        return Field.builder()
                .description(GENERATED_TAN_LABEL)
                .name(GENERATED_TAN_KEY)
                .numeric(false)
                .minLength(1)
                .maxLength(MAX_VALUE)
                .build();
    }

    String selectAuthMethod(List<? extends SelectableMethod> methods)
            throws SupplementalInfoException {
        Map<String, String> supplementalInformation;

        if (methods.size() > 1) {
            supplementalInformation =
                    supplementalInformationHelper.askSupplementalInformation(
                            buildScaMethodsField(methods));
            return getSelectedAuthMethodId(supplementalInformation, methods);
        }
        return methods.get(0).getIdentifier();
    }

    private String getSelectedAuthMethodId(
            Map<String, String> supplementalInformation, List<? extends SelectableMethod> methods) {
        int index = Integer.parseInt(supplementalInformation.get(SELECT_AUTH_METHOD_KEY)) - 1;
        return methods.get(index).getIdentifier();
    }

    private Field buildScaMethodsField(List<? extends SelectableMethod> methods) {
        int maxNumber = methods.size();
        String description =
                IntStream.range(0, maxNumber)
                        .mapToObj(
                                index -> format("(%d) %s", index + 1, methods.get(index).getName()))
                        .collect(joining(";\n"));

        return Field.builder()
                .description(SELECT_AUTH_METHOD_LABEL)
                .helpText(format(SELECT_AUTH_METHOD_INFO, maxNumber).concat(description))
                .name(SELECT_AUTH_METHOD_KEY)
                .numeric(true)
                .minLength(1)
                .pattern(format("([1-%d])", maxNumber))
                .patternError(SELECT_AUTH_METHOD_ERROR_MESSAGE)
                .build();
    }
}
