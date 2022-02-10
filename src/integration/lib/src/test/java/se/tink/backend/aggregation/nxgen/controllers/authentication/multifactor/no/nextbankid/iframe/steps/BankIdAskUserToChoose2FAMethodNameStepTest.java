package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_CHOOSE_2FA_METHOD_OPTION_BUTTON_LABEL;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.mockLocatorDoesNotExists;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.mockLocatorExists;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import lombok.Singular;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchQuery;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class BankIdAskUserToChoose2FAMethodNameStepTest {

    /*
    Mocks
     */
    private WebDriverService webDriver;
    private BankIdScreensManager screensManager;
    private Catalog catalog;
    private SupplementalInformationController supplementalInformationController;

    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private BankIdAskUserToChoose2FAMethodNameStep choose2FAStep;

    @Before
    public void setup() {
        webDriver = mock(WebDriverService.class);
        screensManager = mock(BankIdScreensManager.class);
        catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("anything not empty");
        supplementalInformationController = mock(SupplementalInformationController.class);

        mocksToVerifyInOrder =
                inOrder(webDriver, screensManager, catalog, supplementalInformationController);

        choose2FAStep =
                new BankIdAskUserToChoose2FAMethodNameStep(
                        webDriver, screensManager, catalog, supplementalInformationController);
    }

    @Test
    @Parameters(method = "buttonLabelsParameters")
    public void
            should_click_change_method_link_then_find_all_available_button_labels_and_ask_user_to_choose_one(
                    List<String> availableLabels, String expectedLabelChosenByUser) {
        // given
        mockElementsWithGivenLabelsExist(availableLabels);

        Field expectedSupplementalInfoField =
                BankIdChoose2FAMethodField.build(catalog, availableLabels);
        mockSupplementalInfoResponse(expectedSupplementalInfoField, expectedLabelChosenByUser);

        // when
        String labelChosenByUser = choose2FAStep.choose2FAMethodName();

        // then
        assertThat(labelChosenByUser).isEqualTo(expectedLabelChosenByUser);

        mocksToVerifyInOrder
                .verify(screensManager)
                .waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(BankIdScreen.CHOOSE_2FA_METHOD_SCREEN)
                                .verifyNoErrorScreens(true)
                                .build());
        mocksToVerifyInOrder
                .verify(webDriver)
                .searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(LOC_CHOOSE_2FA_METHOD_OPTION_BUTTON_LABEL)
                                .searchForSeconds(10)
                                .build());

        mocksToVerifyInOrder
                .verify(supplementalInformationController)
                .askSupplementalInformationSync(expectedSupplementalInfoField);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] buttonLabelsParameters() {
        return Stream.of(
                        ChooseMethodTestParams.builder()
                                .label("Method label 1")
                                .label("Method label 2")
                                .label("Method label 3")
                                .chosenLabel("Method label 2")
                                .build(),
                        ChooseMethodTestParams.builder()
                                .label("Method label 1")
                                .label("Method label 2")
                                .chosenLabel("Method label 1")
                                .build())
                .map(ChooseMethodTestParams::toMethodParams)
                .toArray();
    }

    @Builder
    private static class ChooseMethodTestParams {
        @Singular("label")
        private final List<String> labels;

        private final String chosenLabel;

        public Object[] toMethodParams() {
            return new Object[] {labels, chosenLabel};
        }
    }

    @Test
    public void should_throw_illegal_state_exception_when_there_are_no_labels() {
        // given
        mockLocatorDoesNotExists(LOC_CHOOSE_2FA_METHOD_OPTION_BUTTON_LABEL, webDriver);

        // when
        Throwable throwable = catchThrowable(() -> choose2FAStep.choose2FAMethodName());

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No 2FA method option button labels found");

        mocksToVerifyInOrder
                .verify(screensManager)
                .waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(BankIdScreen.CHOOSE_2FA_METHOD_SCREEN)
                                .verifyNoErrorScreens(true)
                                .build());
        mocksToVerifyInOrder
                .verify(webDriver)
                .searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(LOC_CHOOSE_2FA_METHOD_OPTION_BUTTON_LABEL)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    private void mockElementsWithGivenLabelsExist(List<String> labels) {
        List<WebElement> labelElements =
                labels.stream()
                        .map(BankIdTestUtils::mockWebElementWithText)
                        .collect(Collectors.toList());
        mockLocatorExists(LOC_CHOOSE_2FA_METHOD_OPTION_BUTTON_LABEL, labelElements, webDriver);
    }

    private void mockSupplementalInfoResponse(Field field, String label) {
        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenReturn(singletonMap(field.getName(), label));
    }
}
