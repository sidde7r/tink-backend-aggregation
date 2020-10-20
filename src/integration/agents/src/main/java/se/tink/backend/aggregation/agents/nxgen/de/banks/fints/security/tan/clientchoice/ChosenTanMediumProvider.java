package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice;

import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice.exception.ClientAnswerException;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class ChosenTanMediumProvider {
    private static final String TAN_MEDIUM_KEY = "tanMedium";
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Catalog catalog;

    public ChosenTanMediumProvider(
            SupplementalInformationHelper supplementalInformationHelper, Catalog catalog) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.catalog = catalog;
    }

    public String getTanMedium(FinTsDialogContext context) {
        Map<String, String> supplementalInformation;
        List<String> tanMediumList = context.getTanMediumList();
        try {
            supplementalInformation =
                    supplementalInformationHelper.askSupplementalInformation(
                            getFieldForGeneratedTan(tanMediumList));
        } catch (SupplementalInfoException e) {
            throw new ClientAnswerException("Could not get Tan Medium selection", e);
        }

        int index = Integer.parseInt(supplementalInformation.get(TAN_MEDIUM_KEY));
        return tanMediumList.get(index);
    }

    private Field getFieldForGeneratedTan(List<String> tanMediumList) {
        return CommonFields.Selection.build(
                catalog, tanMediumList, new LocalizableKey("Choose TAN Medium you want to use"));
    }
}
