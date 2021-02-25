package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.nemIdMetricsMock;

import java.util.ArrayList;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdInitializeIframeStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdLoginPageStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdPerform2FAStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdVerifyLoginResponseStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod.NemIdChoose2FAMethodStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;

@RunWith(JUnitParamsRunner.class)
public class NemIdIFrameControllerTest {

    private NemIdWebDriverWrapper driverWrapper;
    private NemIdTokenValidator tokenValidator;

    private NemIdInitializeIframeStep initializeIframeStep;
    private NemIdLoginPageStep loginPageStep;
    private NemIdVerifyLoginResponseStep verifyLoginResponseStep;
    private NemIdChoose2FAMethodStep choose2FAMethodStep;
    private NemIdPerform2FAStep perform2FAStep;

    private NemIdIFrameController nemIdIFrameController;

    private Credentials credentials;
    private InOrder mocksToVerifyInOrder;

    @Before
    public void setup() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);
        initializeIframeStep = mock(NemIdInitializeIframeStep.class);
        tokenValidator = mock(NemIdTokenValidator.class);
        loginPageStep = mock(NemIdLoginPageStep.class);
        verifyLoginResponseStep = mock(NemIdVerifyLoginResponseStep.class);
        choose2FAMethodStep = mock(NemIdChoose2FAMethodStep.class);
        perform2FAStep = mock(NemIdPerform2FAStep.class);

        nemIdIFrameController =
                new NemIdIFrameController(
                        driverWrapper,
                        nemIdMetricsMock(),
                        tokenValidator,
                        initializeIframeStep,
                        loginPageStep,
                        verifyLoginResponseStep,
                        choose2FAMethodStep,
                        perform2FAStep);

        credentials = mock(Credentials.class);
        mocksToVerifyInOrder =
                inOrder(
                        driverWrapper,
                        initializeIframeStep,
                        tokenValidator,
                        loginPageStep,
                        verifyLoginResponseStep,
                        choose2FAMethodStep,
                        perform2FAStep);
    }

    @Test
    @Parameters(method = "all2FAMethodScreensAndChosenMethods")
    public void should_execute_all_steps_in_correct_order_and_close_web_driver(
            NemId2FAMethodScreen defaultMethodScreen, NemId2FAMethod methodChosenByUser) {
        // given
        when(verifyLoginResponseStep.checkLoginResultAndGetDefault2FAScreen(any()))
                .thenReturn(defaultMethodScreen);
        when(choose2FAMethodStep.choose2FAMethod(any(), any())).thenReturn(methodChosenByUser);
        when(perform2FAStep.authenticateToGetNemIdToken(any(), any())).thenReturn("SAMPLE TOKEN");

        mockThatTokenIsValid();

        // when
        nemIdIFrameController.logInWithCredentials(credentials);

        // then
        mocksToVerifyInOrder.verify(initializeIframeStep).initializeNemIdIframe(credentials);
        mocksToVerifyInOrder.verify(loginPageStep).login(credentials);
        mocksToVerifyInOrder
                .verify(verifyLoginResponseStep)
                .checkLoginResultAndGetDefault2FAScreen(credentials);
        mocksToVerifyInOrder
                .verify(choose2FAMethodStep)
                .choose2FAMethod(credentials, defaultMethodScreen);
        mocksToVerifyInOrder
                .verify(perform2FAStep)
                .authenticateToGetNemIdToken(methodChosenByUser, credentials);
        mocksToVerifyInOrder.verify(tokenValidator).verifyTokenIsValid("SAMPLE TOKEN");

        mocksToVerifyInOrder.verify(driverWrapper).quitDriver();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] all2FAMethodScreensAndChosenMethods() {
        List<Object[]> paramsList = new ArrayList<>();

        for (NemId2FAMethodScreen methodScreen : NemId2FAMethodScreen.values()) {
            for (NemId2FAMethod chosenMethod : NemId2FAMethod.values()) {
                paramsList.add(new Object[] {methodScreen, chosenMethod});
            }
        }

        return paramsList.toArray(new Object[0]);
    }

    @Test
    @Parameters(method = "all2FAMethodScreensAndChosenMethods")
    public void should_throw_token_validation_error(
            NemId2FAMethodScreen defaultMethodScreen, NemId2FAMethod methodChosenByUser) {
        // given
        when(verifyLoginResponseStep.checkLoginResultAndGetDefault2FAScreen(any()))
                .thenReturn(defaultMethodScreen);
        when(choose2FAMethodStep.choose2FAMethod(any(), any())).thenReturn(methodChosenByUser);
        when(perform2FAStep.authenticateToGetNemIdToken(any(), any()))
                .thenReturn("SAMPLE INVALID TOKEN 12345");

        Throwable tokenValidationError = new RuntimeException("invalid token");
        mockThatTokenIsInvalid(tokenValidationError);

        // when
        Throwable throwable =
                catchThrowable(() -> nemIdIFrameController.logInWithCredentials(credentials));

        // then
        assertThat(throwable).isEqualTo(tokenValidationError);

        mocksToVerifyInOrder.verify(initializeIframeStep).initializeNemIdIframe(credentials);
        mocksToVerifyInOrder.verify(loginPageStep).login(credentials);
        mocksToVerifyInOrder
                .verify(verifyLoginResponseStep)
                .checkLoginResultAndGetDefault2FAScreen(credentials);
        mocksToVerifyInOrder
                .verify(choose2FAMethodStep)
                .choose2FAMethod(credentials, defaultMethodScreen);
        mocksToVerifyInOrder
                .verify(perform2FAStep)
                .authenticateToGetNemIdToken(methodChosenByUser, credentials);
        mocksToVerifyInOrder
                .verify(tokenValidator)
                .verifyTokenIsValid("SAMPLE INVALID TOKEN 12345");

        mocksToVerifyInOrder.verify(driverWrapper).quitDriver();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    private void mockThatTokenIsValid() {
        doNothing().when(tokenValidator).verifyTokenIsValid(anyString());
    }

    private void mockThatTokenIsInvalid(Throwable tokenValidationError) {
        doThrow(tokenValidationError).when(tokenValidator).verifyTokenIsValid(anyString());
    }
}
