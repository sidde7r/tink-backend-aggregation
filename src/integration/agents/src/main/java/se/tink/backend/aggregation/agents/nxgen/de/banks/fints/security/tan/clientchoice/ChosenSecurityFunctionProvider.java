package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.utils.RangeRegex;
import se.tink.libraries.i18n.Catalog;

public class ChosenSecurityFunctionProvider {

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Catalog catalog;

    public ChosenSecurityFunctionProvider(
            SupplementalInformationHelper supplementalInformationHelper, Catalog catalog) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.catalog = catalog;
    }

    public String getChosenSecurityFunction(FinTsDialogContext context) {
        Map<String, String> securityFunctions = context.getAllowedSecurityFunctions();
        return collectScaMethod(securityFunctions);
    }

    private String collectScaMethod(Map<String, String> scaMethods)
            throws SupplementalInfoException {

        int maxNumber = scaMethods.size();

        List<Map.Entry<String, String>> availableChoices = new ArrayList<>(scaMethods.entrySet());
        availableChoices.sort(Comparator.comparing(Map.Entry::getValue));

        String description =
                IntStream.range(0, maxNumber)
                        .mapToObj(
                                i ->
                                        prettyPrintScaMethodWithIndex(
                                                availableChoices.get(i).getValue(), i))
                        .collect(Collectors.joining(";\n"));

        Field scaMethodField =
                Field.builder()
                        .description(this.catalog.getString(description))
                        .helpText("Please select SCA method")
                        .name("chosenScaMethod")
                        .numeric(true)
                        .minLength(1)
                        .maxLength(Integer.toString(maxNumber).length())
                        .hint(String.format("Select from 1 to %d", maxNumber))
                        .pattern(RangeRegex.regexForRange(1, maxNumber))
                        .patternError("The chosen SCA method is not valid")
                        .build();

        Map<String, String> supplementalInformation =
                supplementalInformationHelper.askSupplementalInformation(scaMethodField);
        int selectedIndex =
                Integer.parseInt(supplementalInformation.get(scaMethodField.getName())) - 1;
        return availableChoices.get(selectedIndex).getKey();
    }

    private String prettyPrintScaMethodWithIndex(String scaMethod, int index) {
        return String.format("(%d): %s", index + 1, scaMethod);
    }
}
