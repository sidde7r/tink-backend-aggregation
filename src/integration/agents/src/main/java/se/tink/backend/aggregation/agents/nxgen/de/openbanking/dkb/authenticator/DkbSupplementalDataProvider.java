package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static java.util.stream.Collectors.joining;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.SupplementalStrings;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.utils.RangeRegex;
import se.tink.libraries.i18n.Catalog;

public class DkbSupplementalDataProvider {

    private static final Pattern EXTRACT_STARTCODE_PATTERN = Pattern.compile("Startcode (\\d+)");

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Catalog catalog;

    public DkbSupplementalDataProvider(
            SupplementalInformationHelper supplementalInformationHelper, Catalog catalog) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.catalog = catalog;
    }

    String getTanCode(List<String> challengeData) throws SupplementalInfoException {
        return getTanCode(null, challengeData);
    }

    String getTanCode(String scaMethodName, List<String> challengeData)
            throws SupplementalInfoException {
        return supplementalInformationHelper
                .askSupplementalInformation(getSupplementalFields(scaMethodName, challengeData))
                .get(SupplementalStrings.GENERATED_TAN_FIELD_KEY);
    }

    Field[] getSupplementalFields(String scaMethodName, List<String> challengeData) {
        List<Field> fields = new LinkedList<>();

        extractStartCode(challengeData).ifPresent(s -> fields.add(getStartcodeField(s)));
        fields.add(getGeneratedCodeField(scaMethodName));

        return fields.toArray(new Field[0]);
    }

    private Optional<String> extractStartCode(List<String> challengeData) {
        return challengeData.stream()
                .filter(s -> EXTRACT_STARTCODE_PATTERN.matcher(s).find())
                .map(
                        s -> {
                            Matcher matcher = EXTRACT_STARTCODE_PATTERN.matcher(s);
                            return matcher.find() ? matcher.group(1) : null;
                        })
                .findFirst();
    }

    private Field getStartcodeField(String startcode) {
        return Field.builder()
                .immutable(true)
                .name(SupplementalStrings.STARTCODE_FIELD_KEY)
                .description(SupplementalStrings.STARTCODE_DESCRIPTION)
                .value(startcode)
                .helpText(SupplementalStrings.STARTCODE_HELPTEXT)
                .build();
    }

    private Field getGeneratedCodeField(String scaMethodName) {
        String helpText =
                scaMethodName != null
                        ? String.format(
                                catalog.getString(
                                        SupplementalStrings.GENERATED_TAN_HELPTEXT_FORMAT),
                                scaMethodName)
                        : catalog.getString(SupplementalStrings.GENERATED_TAN_HELPTEXT);

        return Field.builder()
                .name(catalog.getString(SupplementalStrings.GENERATED_TAN_FIELD_KEY))
                .description(catalog.getString(SupplementalStrings.GENERATED_TAN_DESCRIPTION))
                .helpText(helpText)
                .minLength(1)
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
        int index =
                Integer.parseInt(
                                supplementalInformation.get(
                                        SupplementalStrings.SELECT_AUTH_METHOD_FIELD_KEY))
                        - 1;
        return methods.get(index).getIdentifier();
    }

    private Field buildScaMethodsField(List<? extends SelectableMethod> methods) {
        int maxNumber = methods.size();

        String helpText =
                IntStream.range(0, maxNumber)
                        .mapToObj(
                                index ->
                                        String.format(
                                                "(%d) %s", index + 1, methods.get(index).getName()))
                        .collect(joining("\n"));

        return Field.builder()
                .name(SupplementalStrings.SELECT_AUTH_METHOD_FIELD_KEY)
                .description(catalog.getString(SupplementalStrings.SELECT_AUTH_METHOD_DESCRIPTION))
                .hint(
                        String.format(
                                catalog.getString(
                                        SupplementalStrings.SELECT_AUTH_METHOD_HINT_FORMAT),
                                maxNumber))
                .helpText(helpText)
                .numeric(true)
                .minLength(1)
                .maxLength(Integer.toString(maxNumber).length())
                .pattern(RangeRegex.regexForRange(1, maxNumber))
                .patternError(catalog.getString(ErrorMessages.SELECT_AUTH_METHOD_ERROR_MESSAGE))
                .build();
    }
}
