package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.IntStream;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.SupplementalDataKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.SupplementalDataLabels;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class DkbSupplementalDataProvider {

    private final SupplementalInformationHelper supplementalInformationHelper;

    public DkbSupplementalDataProvider(
            SupplementalInformationHelper supplementalInformationHelper) {
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    String getTanCode(List<String> challengeData) throws SupplementalInfoException {
        return supplementalInformationHelper
                .askSupplementalInformation(getFieldForGeneratedTan(challengeData))
                .get(SupplementalDataKeys.GENERATED_TAN_KEY);
    }

    Field getFieldForGeneratedTan(List<String> challengeData) {
        return Field.builder()
                .description(getDescription(challengeData))
                .name(SupplementalDataKeys.GENERATED_TAN_KEY)
                .numeric(false)
                .minLength(1)
                .maxLength(MAX_VALUE)
                .build();
    }

    private String getDescription(List<String> challengeData) {
        return challengeData.stream()
                .filter(s -> SupplementalDataLabels.STARTCODE_CHIP_PATTERN.matcher(s).find())
                .map(
                        s -> {
                            Matcher matcher =
                                    SupplementalDataLabels.STARTCODE_CHIP_PATTERN.matcher(s);
                            matcher.find();
                            return matcher.group(1);
                        })
                .findFirst()
                .map(s -> String.format(SupplementalDataLabels.CHIP_TAN_DESCRIPTION_LABEL, s))
                .orElse(SupplementalDataLabels.GENERATED_TAN_LABEL);
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
        int index =
                Integer.parseInt(
                                supplementalInformation.get(
                                        SupplementalDataKeys.SELECT_AUTH_METHOD_KEY))
                        - 1;
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
                .description(SupplementalDataLabels.SELECT_AUTH_METHOD_LABEL)
                .helpText(
                        format(SupplementalDataLabels.SELECT_AUTH_METHOD_INFO, maxNumber)
                                .concat(description))
                .name(SupplementalDataKeys.SELECT_AUTH_METHOD_KEY)
                .numeric(true)
                .minLength(1)
                .pattern(format("([1-%d])", maxNumber))
                .patternError(ErrorMessages.SELECT_AUTH_METHOD_ERROR_MESSAGE)
                .build();
    }
}
