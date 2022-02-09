package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.THIRD_PARTY_APP_TIMEOUT_IN_SECONDS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.ENTER_BANK_ID_PASSWORD_SCREEN;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.aggregation.agents.utils.supplementalfields.NorwegianFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class BankIdAuthWithThirdPartyAppStepTest {

    /*
    Mocks
     */
    private BankIdScreensManager screensManager;
    private Catalog catalog;
    private SupplementalInformationController supplementalInformationController;

    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private BankIdAuthWithThirdPartyAppStep authWithBankIdAppStep;

    @Before
    public void setup() {
        screensManager = mock(BankIdScreensManager.class);
        catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("whatever");
        supplementalInformationController = mock(SupplementalInformationController.class);

        mocksToVerifyInOrder = inOrder(screensManager, supplementalInformationController);

        authWithBankIdAppStep =
                new BankIdAuthWithThirdPartyAppStep(
                        screensManager, catalog, supplementalInformationController);
    }

    @Test
    public void should_ask_user_to_confirm_third_party_app_and_wait_for_password_screen() {
        // given
        mockUserAnswersSupplementalInfo();

        // when
        authWithBankIdAppStep.authenticateWithThirdPartyApp(true);

        // then
        mocksToVerifyInOrder
                .verify(supplementalInformationController)
                .askSupplementalInformationSync(NorwegianFields.BankIdAppField.build(catalog));
        mocksToVerifyInOrder
                .verify(screensManager)
                .waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(ENTER_BANK_ID_PASSWORD_SCREEN)
                                .waitForSeconds(THIRD_PARTY_APP_TIMEOUT_IN_SECONDS)
                                .verifyNoErrorScreens(true)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    private void mockUserAnswersSupplementalInfo() {
        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenReturn(Collections.emptyMap());
    }
}
