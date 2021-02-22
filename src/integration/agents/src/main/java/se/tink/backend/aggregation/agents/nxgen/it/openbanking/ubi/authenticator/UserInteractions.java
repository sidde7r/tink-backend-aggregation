package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@RequiredArgsConstructor
public class UserInteractions {

    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;

    private static final LocalizableKey DESCRIPTION =
            new LocalizableKey("Please open the bank application and confirm the order.");
    private static final String FIELD_NAME = "name";
    private static final LocalizableKey VALUE = new LocalizableKey("waiting for confirmation");

    private static final long PROMPT_WAIT_FOR_MINUTES = 2;

    void displayPromptAndWaitForAcceptance() {
        Field field =
                CommonFields.Information.build(
                        FIELD_NAME, catalog.getString(VALUE), catalog.getString(DESCRIPTION), "");
        String mfaId = supplementalInformationController.askSupplementalInformationAsync(field);
        supplementalInformationController.waitForSupplementalInformation(
                mfaId, PROMPT_WAIT_FOR_MINUTES, TimeUnit.MINUTES);
    }
}
