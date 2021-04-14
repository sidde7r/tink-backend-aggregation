package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_REFERENCE_WORDS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_SUBMIT_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.MOBILE_BANK_ID_TIMEOUT_IN_SECONDS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.mockLocatorDoesNotExists;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.mockLocatorExists;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.ENTER_BANK_ID_PASSWORD_SCREEN;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.utils.supplementalfields.NorwegianFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class BankIdAuthWithMobileBankIdStepTest {

    /*
    Mocks
     */
    private BankIdWebDriver driver;
    private BankIdScreensManager screensManager;
    private Catalog catalog;
    private SupplementalInformationController supplementalInformationController;

    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private BankIdAuthWithMobileBankIdStep authWithMobileBankIdStep;

    @Before
    public void setup() {
        driver = mock(BankIdWebDriver.class);
        screensManager = mock(BankIdScreensManager.class);
        catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("whatever");
        supplementalInformationController = mock(SupplementalInformationController.class);

        mocksToVerifyInOrder = inOrder(driver, screensManager, supplementalInformationController);

        authWithMobileBankIdStep =
                new BankIdAuthWithMobileBankIdStep(
                        driver, screensManager, catalog, supplementalInformationController);
    }

    @Test
    public void
            should_send_request_then_display_reference_words_to_user_and_wait_for_password_screen() {
        // given
        mockUserAnswersSupplementalInfo();

        WebElement referenceWordsElement =
                BankIdTestUtils.mockWebElementWithText("Reference words 123");
        mockLocatorExists(LOC_REFERENCE_WORDS, referenceWordsElement, driver);

        // when
        authWithMobileBankIdStep.authenticateWithMobileBankId();

        // then
        mocksToVerifyInOrder.verify(driver).clickButton(LOC_SUBMIT_BUTTON);
        mocksToVerifyInOrder
                .verify(driver)
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(LOC_REFERENCE_WORDS)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder
                .verify(supplementalInformationController)
                .askSupplementalInformationSync(
                        NorwegianFields.BankIdReferenceInfo.build(catalog, "Reference words 123"));
        mocksToVerifyInOrder
                .verify(screensManager)
                .waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(ENTER_BANK_ID_PASSWORD_SCREEN)
                                .waitForSeconds(MOBILE_BANK_ID_TIMEOUT_IN_SECONDS)
                                .verifyNoErrorScreens(true)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_exception_when_reference_words_cannot_be_found() {
        // given
        mockUserAnswersSupplementalInfo();
        mockLocatorDoesNotExists(LOC_REFERENCE_WORDS, driver);

        // when
        Throwable throwable =
                catchThrowable(() -> authWithMobileBankIdStep.authenticateWithMobileBankId());

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not find reference words");

        mocksToVerifyInOrder.verify(driver).clickButton(LOC_SUBMIT_BUTTON);
        mocksToVerifyInOrder
                .verify(driver)
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(LOC_REFERENCE_WORDS)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    private void mockUserAnswersSupplementalInfo() {
        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenReturn(Collections.emptyMap());
    }
}
