package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codeapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_TIMEOUT_ICON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NOT_EMPTY_NEMID_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.nemIdMetricsMock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.webElementMock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.webElementMockWithText;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;

public class NemIdCodeAppCollectTokenStepTest {

    private NemIdWebDriverWrapper driverWrapper;

    private NemIdCodeAppCollectTokenStep collectTokenStep;

    private InOrder mocksToVerifyInOrder;

    @Before
    public void setup() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);
        mocksToVerifyInOrder = inOrder(driverWrapper);

        collectTokenStep = new NemIdCodeAppCollectTokenStep(driverWrapper, nemIdMetricsMock());
    }

    @Test
    public void should_return_correct_token() {
        // given
        WebElement tokenElement = webElementMockWithText("--- SAMPLE TOKEN ---");
        when(driverWrapper.searchForFirstElement(any()))
                .thenReturn(ElementsSearchResult.of(NOT_EMPTY_NEMID_TOKEN, tokenElement));

        // when
        String nemIdToken = collectTokenStep.collectToken();

        // then
        assertThat(nemIdToken).isEqualTo("--- SAMPLE TOKEN ---");

        verifySearchingForAllRelevantElements();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_timeout_exception_when_timeout_icon_is_found() {
        // given
        WebElement timeoutElement = webElementMock();
        when(driverWrapper.searchForFirstElement(any()))
                .thenReturn(ElementsSearchResult.of(NEMID_TIMEOUT_ICON, timeoutElement));

        // when
        Throwable throwable = catchThrowable(() -> collectTokenStep.collectToken());

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, NemIdError.TIMEOUT.exception());

        verifySearchingForAllRelevantElements();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void
            should_try_to_find_token_or_timeout_for_a_little_longer_than_nem_id_timeout_and_then_fail() {
        // given
        when(driverWrapper.searchForFirstElement(any())).thenReturn(ElementsSearchResult.empty());

        // when
        Throwable throwable = catchThrowable(() -> collectTokenStep.collectToken());

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, LoginError.CREDENTIALS_VERIFICATION_ERROR.exception());

        verifySearchingForAllRelevantElements();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    private void verifySearchingForAllRelevantElements() {
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInParentWindow(NOT_EMPTY_NEMID_TOKEN)
                                .searchInAnIframe(NEMID_TIMEOUT_ICON)
                                .searchForSeconds(NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN)
                                .build());
    }
}
