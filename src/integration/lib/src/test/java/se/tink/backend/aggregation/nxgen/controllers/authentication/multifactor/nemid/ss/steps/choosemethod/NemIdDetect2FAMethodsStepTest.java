package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_SELECT_METHOD_POPUP;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod.CODE_APP;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod.CODE_CARD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod.CODE_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethodScreen.CODE_APP_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod.NemIdDetect2FAMethodsStep.ELEMENTS_TO_SEARCH_FOR_AFTER_CLICKING_CHANGE_METHOD_LINK;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod.NemIdSelect2FAPopupOptionButton.CODE_APP_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod.NemIdSelect2FAPopupOptionButton.CODE_CARD_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod.NemIdSelect2FAPopupOptionButton.CODE_TOKEN_BUTTON;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethodScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper;

@RunWith(JUnitParamsRunner.class)
public class NemIdDetect2FAMethodsStepTest {

    private NemIdWebDriverWrapper driverWrapper;
    private InOrder mocksToVerifyInOrder;

    private NemIdDetect2FAMethodsStep detect2FAMethodsStep;

    @Before
    public void setup() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);
        mocksToVerifyInOrder = inOrder(driverWrapper);

        detect2FAMethodsStep = new NemIdDetect2FAMethodsStep(driverWrapper);
    }

    @Test
    @Parameters(method = "all2FAScreensTestParams")
    public void
            should_return_cannot_change_method_result_and_stay_on_default_screen_when_there_is_no_change_method_link(
                    NemId2FAMethodScreen defaultScreen) {
        // given
        mockElementDoesntExist(NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD);

        // when
        NemIdDetect2FAMethodsResult detect2FMethodsResult =
                detect2FAMethodsStep.detect2FAMethods(defaultScreen);

        // then
        assertThat(detect2FMethodsResult)
                .isEqualToComparingFieldByFieldRecursively(
                        NemIdDetect2FAMethodsResult.canOnlyUseDefaultMethod(defaultScreen));

        verifyTriesToFindElement(NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD);
        mocksToVerifyInOrder.verify(driverWrapper).getFullPageSourceLog();

        verifyNoMoreClicking();
        verifyNoMoreInteractionsWithMocks();
    }

    @Test
    @Parameters(method = "all2FAScreensTestParams")
    public void
            should_return_cannot_change_method_result_and_stay_on_default_screen_when_link_doesnt_change_screen_nor_open_popup(
                    NemId2FAMethodScreen defaultScreen) {
        // given
        mockElementDoesExist(NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD);
        mockElementsSearchResultAfterLinkIsClicked(defaultScreen.getSelectorToDetectScreen());

        // when
        NemIdDetect2FAMethodsResult detect2FMethodsResult =
                detect2FAMethodsStep.detect2FAMethods(defaultScreen);

        // then
        assertThat(detect2FMethodsResult)
                .isEqualToComparingFieldByFieldRecursively(
                        NemIdDetect2FAMethodsResult.canOnlyUseDefaultMethod(defaultScreen));

        verifyTriesToFindElement(NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD);
        verifyIsSearchingForFirstOfElements(
                ELEMENTS_TO_SEARCH_FOR_AFTER_CLICKING_CHANGE_METHOD_LINK);
        mocksToVerifyInOrder.verify(driverWrapper).getFullPageSourceLog();

        verifyNoMoreClicking();
        verifyNoMoreInteractionsWithMocks();
    }

    @Test
    @Parameters(method = "allPairwiseDifferent2FAScreensTestParams")
    public void
            should_return_can_toggle_between_methods_result_and_stay_on_new_screen_when_link_toggles_method_screens(
                    NemId2FAMethodScreen defaultScreen,
                    NemId2FAMethodScreen screenAfterClickingLink) {
        // given
        mockElementDoesExist(NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD);
        mockElementsSearchResultAfterLinkIsClicked(
                screenAfterClickingLink.getSelectorToDetectScreen());

        // when
        NemIdDetect2FAMethodsResult detect2FMethodsResult =
                detect2FAMethodsStep.detect2FAMethods(defaultScreen);

        // then
        assertThat(detect2FMethodsResult)
                .isEqualToComparingFieldByFieldRecursively(
                        NemIdDetect2FAMethodsResult.canToggleBetween2Methods(
                                defaultScreen, screenAfterClickingLink));

        verifyTriesToFindElement(NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD);
        verifyIsSearchingForFirstOfElements(
                ELEMENTS_TO_SEARCH_FOR_AFTER_CLICKING_CHANGE_METHOD_LINK);

        verifyNoMoreClicking();
        verifyNoMoreInteractionsWithMocks();
    }

    @Test
    @Parameters(method = "all2FAScreensTestParams")
    public void
            should_return_can_choose_method_from_popup_and_stay_on_default_screen_when_popup_is_open(
                    NemId2FAMethodScreen defaultScreen) {
        // given
        mockElementDoesExist(NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD);
        mockElementsSearchResultAfterLinkIsClicked(NEMID_SELECT_METHOD_POPUP);
        mockElementsSearchResultForAllOptionButtons(
                ImmutableMap.of(
                        CODE_APP_BUTTON, true,
                        CODE_CARD_BUTTON, true,
                        CODE_TOKEN_BUTTON, true));

        // when
        NemIdDetect2FAMethodsResult detect2FMethodsResult =
                detect2FAMethodsStep.detect2FAMethods(defaultScreen);

        // then
        assertThat(detect2FMethodsResult)
                .isEqualToComparingFieldByFieldRecursively(
                        NemIdDetect2FAMethodsResult.canChooseMethodFromPopup(
                                defaultScreen, ImmutableSet.of(CODE_APP, CODE_CARD, CODE_TOKEN)));

        verifyTriesToFindElement(NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD);
        verifyIsSearchingForFirstOfElements(
                ELEMENTS_TO_SEARCH_FOR_AFTER_CLICKING_CHANGE_METHOD_LINK);
        verifyIsSearchingForAllOfElements(
                NemIdSelect2FAPopupOptionButton.getSelectorsForAllButtons());

        verifyNoMoreClicking();
        verifyNoMoreInteractionsWithMocks();
    }

    @Test
    @Parameters(method = "popupButtonsTestParams")
    public void
            should_return_can_choose_method_from_popup_result_with_available_methods_list_based_only_on_visible_popup_buttons(
                    NemId2FAMethodScreen defaultScreen,
                    Map<NemIdSelect2FAPopupOptionButton, Boolean>
                            existingButtonsWithIsDisplayedFlag,
                    Set<NemId2FAMethod> expectedAvailableMethods) {
        // given
        mockElementDoesExist(NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD);
        mockElementsSearchResultAfterLinkIsClicked(NEMID_SELECT_METHOD_POPUP);
        mockElementsSearchResultForAllOptionButtons(existingButtonsWithIsDisplayedFlag);

        // when
        NemIdDetect2FAMethodsResult detect2FMethodsResult =
                detect2FAMethodsStep.detect2FAMethods(defaultScreen);

        // then
        assertThat(detect2FMethodsResult)
                .isEqualToComparingFieldByFieldRecursively(
                        NemIdDetect2FAMethodsResult.canChooseMethodFromPopup(
                                defaultScreen, expectedAvailableMethods));
    }

    @SuppressWarnings("unused")
    private Object[] popupButtonsTestParams() {
        return Stream.of(
                        PopupButtonsTestParams.builder()
                                .defaultScreen(CODE_APP_SCREEN)
                                .existingButtonsWithIsButtonDisplayed(
                                        ImmutableMap.of(CODE_CARD_BUTTON, true))
                                .expectedAvailableMethods(ImmutableSet.of(CODE_APP, CODE_CARD))
                                .build(),
                        PopupButtonsTestParams.builder()
                                .defaultScreen(CODE_APP_SCREEN)
                                .existingButtonsWithIsButtonDisplayed(
                                        ImmutableMap.of(
                                                CODE_CARD_BUTTON, true, CODE_TOKEN_BUTTON, false))
                                .expectedAvailableMethods(ImmutableSet.of(CODE_APP, CODE_CARD))
                                .build(),
                        PopupButtonsTestParams.builder()
                                .defaultScreen(CODE_APP_SCREEN)
                                .existingButtonsWithIsButtonDisplayed(
                                        ImmutableMap.of(
                                                CODE_CARD_BUTTON, true, CODE_TOKEN_BUTTON, true))
                                .expectedAvailableMethods(
                                        ImmutableSet.of(CODE_APP, CODE_CARD, CODE_TOKEN))
                                .build(),
                        PopupButtonsTestParams.builder()
                                .defaultScreen(CODE_APP_SCREEN)
                                .existingButtonsWithIsButtonDisplayed(
                                        ImmutableMap.of(
                                                CODE_CARD_BUTTON, false, CODE_TOKEN_BUTTON, false))
                                .expectedAvailableMethods(ImmutableSet.of(CODE_APP))
                                .build())
                .map(PopupButtonsTestParams::toMethodParams)
                .toArray(Object[]::new);
    }

    @Data
    @Builder
    private static class PopupButtonsTestParams {
        private final NemId2FAMethodScreen defaultScreen;
        private final Map<NemIdSelect2FAPopupOptionButton, Boolean>
                existingButtonsWithIsButtonDisplayed;
        private final Set<NemId2FAMethod> expectedAvailableMethods;

        private Object[] toMethodParams() {
            return new Object[] {
                defaultScreen, existingButtonsWithIsButtonDisplayed, expectedAvailableMethods
            };
        }
    }

    @SuppressWarnings("unused")
    private Object[] all2FAScreensTestParams() {
        return NemId2FAMethodScreen.values();
    }

    @SuppressWarnings("unused")
    private Object[] allPairwiseDifferent2FAScreensTestParams() {
        return NemIdTestHelper.allNemId2FAPairwiseDifferentScreens().stream()
                .map(tuple -> new Object[] {tuple._1, tuple._2})
                .toArray(Object[]::new);
    }

    @SuppressWarnings("SameParameterValue")
    private void mockElementDoesntExist(By elementSelector) {
        when(driverWrapper.tryFindElement(elementSelector)).thenReturn(Optional.empty());
    }

    @SuppressWarnings("SameParameterValue")
    private void mockElementDoesExist(By elementSelector) {
        WebElement element = NemIdTestHelper.webElementMock();
        when(driverWrapper.tryFindElement(elementSelector)).thenReturn(Optional.of(element));
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyTriesToFindElement(By elementSelector) {
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(elementSelector);
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyIsSearchingForFirstOfElements(List<By> elementSelectors) {
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForFirstElement(
                        ElementsSearchQuery.builder().searchInAnIframe(elementSelectors).build());
    }

    private void verifyIsSearchingForAllOfElements(List<By> elementSelectors) {
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForAllElements(
                        ElementsSearchQuery.builder().searchInAnIframe(elementSelectors).build());
    }

    private void verifyNoMoreClicking() {
        mocksToVerifyInOrder.verify(driverWrapper, times(0)).clickButton(any());
    }

    private void verifyNoMoreInteractionsWithMocks() {
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    private void mockElementsSearchResultAfterLinkIsClicked(By elementToBeFoundSelector) {
        WebElement element = NemIdTestHelper.webElementMock();
        when(driverWrapper.searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInAnIframe(
                                        ELEMENTS_TO_SEARCH_FOR_AFTER_CLICKING_CHANGE_METHOD_LINK)
                                .build()))
                .thenReturn(ElementsSearchResult.of(elementToBeFoundSelector, element));
    }

    private void mockElementsSearchResultForAllOptionButtons(
            Map<NemIdSelect2FAPopupOptionButton, Boolean> buttonsWithIsButtonDisplayed) {

        List<ElementsSearchResult> searchResults = new ArrayList<>();

        for (NemIdSelect2FAPopupOptionButton optionButton :
                NemIdSelect2FAPopupOptionButton.values()) {

            if (!buttonsWithIsButtonDisplayed.containsKey(optionButton)) {
                continue;
            }

            boolean isDisplayed = buttonsWithIsButtonDisplayed.get(optionButton);
            WebElement element = NemIdTestHelper.webElementMock(isDisplayed);

            searchResults.add(ElementsSearchResult.of(optionButton.getButtonSelector(), element));
        }

        when(driverWrapper.searchForAllElements(
                        ElementsSearchQuery.builder()
                                .searchInAnIframe(
                                        NemIdSelect2FAPopupOptionButton.getSelectorsForAllButtons())
                                .build()))
                .thenReturn(searchResults);
    }
}
