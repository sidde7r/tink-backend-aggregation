package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.IFRAME;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.USERNAME_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.nemIdMetricsMock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.verifyNTimes;

import java.util.Base64;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParameters;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParametersFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;

public class NemIdInitializeIframeStepTest {

    private static final String SAMPLE_NEM_ID_FETCHED_ELEMENTS = "--- SAMPLE NEM ID ELEMENTS ---";

    private NemIdWebDriverWrapper driverWrapper;
    private NemIdCredentialsStatusUpdater statusUpdater;
    private NemIdParametersFetcher nemIdParametersFetcher;

    private NemIdInitializeIframeStep initializeIframeStep;

    private Credentials credentials;
    private InOrder mocksToVerifyInOrder;

    @Before
    public void setup() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);
        statusUpdater = mock(NemIdCredentialsStatusUpdater.class);

        nemIdParametersFetcher = mock(NemIdParametersFetcher.class);
        when(nemIdParametersFetcher.getNemIdParameters())
                .thenReturn(new NemIdParameters(SAMPLE_NEM_ID_FETCHED_ELEMENTS));

        initializeIframeStep =
                new NemIdInitializeIframeStep(
                        driverWrapper, nemIdMetricsMock(), statusUpdater, nemIdParametersFetcher);

        credentials = mock(Credentials.class);
        mocksToVerifyInOrder = inOrder(nemIdParametersFetcher, driverWrapper, statusUpdater);
    }

    private void verifyCorrectIframeInitialization() {
        mocksToVerifyInOrder.verify(nemIdParametersFetcher).getNemIdParameters();
        mocksToVerifyInOrder.verify(driverWrapper).get(NemIdConstants.NEM_ID_APPLET_URL);

        mocksToVerifyInOrder.verify(driverWrapper).switchToParentWindow();

        String htmlToInject =
                Optional.of(String.format(NemIdConstants.BASE_HTML, SAMPLE_NEM_ID_FETCHED_ELEMENTS))
                        .map(String::getBytes)
                        .map(Base64.getEncoder()::encodeToString)
                        .orElseThrow(IllegalStateException::new);
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .executeScript("document.write(atob(\"" + htmlToInject + "\"));");
    }

    @Test
    public void
            should_properly_initialize_iframe_then_wait_for_iframe_and_user_input_and_update_status_payload() {
        // given
        mockElementExists(IFRAME);
        mockElementExists(USERNAME_INPUT);

        // when
        initializeIframeStep.initializeNemIdIframe(credentials);

        // then
        verifyCorrectIframeInitialization();
        mocksToVerifyInOrder.verify(driverWrapper).waitForElement(IFRAME, 15);
        mocksToVerifyInOrder.verify(driverWrapper).trySwitchToNemIdIframe();
        mocksToVerifyInOrder.verify(driverWrapper).waitForElement(USERNAME_INPUT, 15);

        statusUpdater.updateStatusPayload(credentials, UserMessage.NEM_ID_PROCESS_INIT);
    }

    @Test
    public void should_try_and_fail_5_times_when_there_is_no_iframe() {
        // given
        mockElementDoesntExist(IFRAME);

        // when
        Throwable throwable =
                catchThrowable(() -> initializeIframeStep.initializeNemIdIframe(credentials));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(NEM_ID_PREFIX + " Can't instantiate iframe element with NemId form.");

        verifyNTimes(
                () -> {
                    verifyCorrectIframeInitialization();
                    mocksToVerifyInOrder.verify(driverWrapper).waitForElement(IFRAME, 15);
                },
                5);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_try_and_fail_5_times_when_there_is_an_iframe_but_no_username_input() {
        // given
        mockElementExists(IFRAME);
        mockElementDoesntExist(USERNAME_INPUT);

        // when
        Throwable throwable =
                catchThrowable(() -> initializeIframeStep.initializeNemIdIframe(credentials));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(NEM_ID_PREFIX + " Can't instantiate iframe element with NemId form.");

        verifyNTimes(
                () -> {
                    verifyCorrectIframeInitialization();
                    mocksToVerifyInOrder.verify(driverWrapper).waitForElement(IFRAME, 15);
                    mocksToVerifyInOrder.verify(driverWrapper).trySwitchToNemIdIframe();
                    mocksToVerifyInOrder.verify(driverWrapper).waitForElement(USERNAME_INPUT, 15);
                },
                5);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    private void mockElementExists(By by) {
        when(driverWrapper.waitForElement(eq(by), anyInt()))
                .thenReturn(Optional.of(mock(WebElement.class)));
    }

    private void mockElementDoesntExist(By by) {
        when(driverWrapper.waitForElement(eq(by), anyInt())).thenReturn(Optional.empty());
    }
}
