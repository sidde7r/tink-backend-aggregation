package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.SelectOption;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NemIdChoose2FAMethodField {

    public static final String FIELD_KEY = "selectAuthMethodField";

    public static final LocalizableKey DESCRIPTION =
            new LocalizableKey("Select how you want to confirm the NemID authentication");

    public static Field build(Catalog catalog, List<NemId2FAMethod> nemId2FAMethods) {
        return CommonFields.Selection.build(
                catalog, DESCRIPTION, prepareSelectOptions(catalog, nemId2FAMethods));
    }

    private static List<SelectOption> prepareSelectOptions(
            Catalog catalog, List<NemId2FAMethod> nemId2FAMethods) {
        return nemId2FAMethods.stream()
                .map(method -> prepareSelectOption(catalog, method))
                .collect(Collectors.toList());
    }

    private static SelectOption prepareSelectOption(
            Catalog catalog, NemId2FAMethod nemId2FAMethod) {
        String optionText = catalog.getString(nemId2FAMethod.getUserFriendlyName());
        String optionKey = nemId2FAMethod.getSupplementalInfoKey();
        String iconUrl = nemId2FAMethod.getSupplementalInfoIconUrl();
        return new SelectOption(optionText, optionKey, iconUrl);
    }
}
