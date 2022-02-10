package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.SelectOption;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class BankIdChoose2FAMethodField {

    public static final String FIELD_KEY = "chooseBankId2FAMethod";

    private static final LocalizableKey DESCRIPTION =
            new LocalizableKey("Select how you want to confirm the BankID authentication");

    public static Field build(Catalog catalog, Collection<String> optionLabels) {
        return CommonFields.Selection.build(
                catalog, DESCRIPTION, prepareSelectOptions(optionLabels));
    }

    private static List<SelectOption> prepareSelectOptions(Collection<String> optionLabels) {
        return optionLabels.stream()
                .map(methodLabel -> new SelectOption(methodLabel, methodLabel))
                .collect(Collectors.toList());
    }
}
