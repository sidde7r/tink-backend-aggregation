package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RequiredArgsConstructor
public class UserInteractions {

    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;

    private static final LocalizableKey INSTRUCTIONS =
            new LocalizableKey("Please open the bank application and confirm the order.");

    public void displayPromptAndWaitForAcceptance() {
        Field field = CommonFields.Instruction.build(catalog.getString(INSTRUCTIONS));
        try {
            supplementalInformationController.askSupplementalInformationSync(field);
        } catch (SupplementalInfoException exception) {
            // We do not care about this exception, since we will double check the consent status
            // even if no supplemental info comes back from end user
        }
    }
}
