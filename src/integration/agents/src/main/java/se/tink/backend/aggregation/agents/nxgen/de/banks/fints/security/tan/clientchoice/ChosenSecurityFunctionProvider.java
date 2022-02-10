package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.i18n_aggregation.Catalog;

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
        List<Map.Entry<String, String>> availableChoices = new ArrayList<>(scaMethods.entrySet());
        availableChoices.sort(Comparator.comparing(Map.Entry::getValue));

        Field scaMethodField =
                CommonFields.Selection.build(
                        catalog,
                        availableChoices.stream()
                                .map(Entry::getValue)
                                .collect(Collectors.toList()));

        Map<String, String> supplementalInformation =
                supplementalInformationHelper.askSupplementalInformation(scaMethodField);
        int selectedIndex =
                Integer.parseInt(supplementalInformation.get(scaMethodField.getName())) - 1;
        return availableChoices.get(selectedIndex).getKey();
    }
}
