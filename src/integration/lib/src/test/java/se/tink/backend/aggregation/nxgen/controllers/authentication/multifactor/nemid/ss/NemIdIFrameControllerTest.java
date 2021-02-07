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
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdInitializeIframeStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdLoginPageStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdVerifyLoginResponseStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codeapp.NemIdAuthorizeWithCodeAppStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codecard.NemIdAuthorizeWithCodeCardStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;

public class NemIdIFrameControllerTest {

    private NemIdWebDriverWrapper driverWrapper;
    private NemIdTokenValidator tokenValidator;

    private NemIdInitializeIframeStep initializeIframeStep;
    private NemIdLoginPageStep loginPageStep;
    private NemIdVerifyLoginResponseStep verifyLoginResponseStep;

    private NemIdAuthorizeWithCodeAppStep authorizeWithCodeAppStep;
    private NemIdAuthorizeWithCodeCardStep authorizeWithCodeCardStep;

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
        authorizeWithCodeAppStep = mock(NemIdAuthorizeWithCodeAppStep.class);
        authorizeWithCodeCardStep = mock(NemIdAuthorizeWithCodeCardStep.class);

        nemIdIFrameController =
                new NemIdIFrameController(
                        driverWrapper,
                        nemIdMetricsMock(),
                        tokenValidator,
                        initializeIframeStep,
                        loginPageStep,
                        verifyLoginResponseStep,
                        authorizeWithCodeAppStep,
                        authorizeWithCodeCardStep);

        credentials = mock(Credentials.class);
        mocksToVerifyInOrder =
                inOrder(
                        driverWrapper,
                        initializeIframeStep,
                        tokenValidator,
                        loginPageStep,
                        verifyLoginResponseStep,
                        authorizeWithCodeAppStep,
                        authorizeWithCodeCardStep);
    }

    @Test
    public void should_execute_all_steps_in_correct_order_and_close_web_driver_for_code_app_flow() {
        // given
        when(verifyLoginResponseStep.checkLoginResultAndGetAvailable2FAMethod(any()))
                .thenReturn(NemId2FAMethod.CODE_APP);

        when(authorizeWithCodeAppStep.getNemIdTokenWithCodeAppAuth(any()))
                .thenReturn("SAMPLE TOKEN");

        mockThatTokenIsValid();

        // when
        nemIdIFrameController.logInWithCredentials(credentials);

        // then
        mocksToVerifyInOrder.verify(initializeIframeStep).initializeNemIdIframe(credentials);
        mocksToVerifyInOrder.verify(loginPageStep).login(credentials);
        mocksToVerifyInOrder
                .verify(verifyLoginResponseStep)
                .checkLoginResultAndGetAvailable2FAMethod(credentials);
        mocksToVerifyInOrder
                .verify(authorizeWithCodeAppStep)
                .getNemIdTokenWithCodeAppAuth(credentials);
        mocksToVerifyInOrder.verify(tokenValidator).verifyTokenIsValid("SAMPLE TOKEN");

        mocksToVerifyInOrder.verify(driverWrapper).quitDriver();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void
            should_execute_all_steps_in_correct_order_and_close_web_driver_for_code_card_flow() {
        // given
        when(verifyLoginResponseStep.checkLoginResultAndGetAvailable2FAMethod(any()))
                .thenReturn(NemId2FAMethod.CODE_CARD);

        when(authorizeWithCodeCardStep.getNemIdTokenWithCodeCardAuth(any()))
                .thenReturn("SAMPLE TOKEN 123");

        mockThatTokenIsValid();

        // when
        nemIdIFrameController.logInWithCredentials(credentials);

        // then
        mocksToVerifyInOrder.verify(initializeIframeStep).initializeNemIdIframe(credentials);
        mocksToVerifyInOrder.verify(loginPageStep).login(credentials);
        mocksToVerifyInOrder
                .verify(verifyLoginResponseStep)
                .checkLoginResultAndGetAvailable2FAMethod(credentials);
        mocksToVerifyInOrder
                .verify(authorizeWithCodeCardStep)
                .getNemIdTokenWithCodeCardAuth(credentials);
        mocksToVerifyInOrder.verify(tokenValidator).verifyTokenIsValid("SAMPLE TOKEN 123");

        mocksToVerifyInOrder.verify(driverWrapper).quitDriver();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_code_token_not_supported_and_close_web_driver_for_code_token_flow() {
        // given
        when(verifyLoginResponseStep.checkLoginResultAndGetAvailable2FAMethod(any()))
                .thenReturn(NemId2FAMethod.CODE_TOKEN);

        when(authorizeWithCodeCardStep.getNemIdTokenWithCodeCardAuth(any()))
                .thenReturn("--SAMPLE TOKEN 123");
        mockThatTokenIsValid();

        // when
        Throwable throwable =
                catchThrowable(() -> nemIdIFrameController.logInWithCredentials(credentials));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, NemIdError.CODE_TOKEN_NOT_SUPPORTED.exception());

        mocksToVerifyInOrder.verify(initializeIframeStep).initializeNemIdIframe(credentials);
        mocksToVerifyInOrder.verify(loginPageStep).login(credentials);
        mocksToVerifyInOrder
                .verify(verifyLoginResponseStep)
                .checkLoginResultAndGetAvailable2FAMethod(credentials);

        mocksToVerifyInOrder.verify(driverWrapper).quitDriver();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_token_validation_error() {
        // given
        when(verifyLoginResponseStep.checkLoginResultAndGetAvailable2FAMethod(any()))
                .thenReturn(NemId2FAMethod.CODE_CARD);

        when(authorizeWithCodeCardStep.getNemIdTokenWithCodeCardAuth(any()))
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
                .checkLoginResultAndGetAvailable2FAMethod(credentials);
        mocksToVerifyInOrder
                .verify(authorizeWithCodeCardStep)
                .getNemIdTokenWithCodeCardAuth(credentials);
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
