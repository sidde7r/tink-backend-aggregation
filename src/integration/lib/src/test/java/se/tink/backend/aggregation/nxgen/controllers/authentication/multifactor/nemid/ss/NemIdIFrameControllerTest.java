package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.nemIdMetricsMock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdCollectTokenStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdInitializeIframeStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdLoginPageStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdVerifyLoginResponseStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdWaitForCodeAppResponseStep;

public class NemIdIFrameControllerTest {

    private NemIdWebDriverWrapper driverWrapper;
    private NemIdInitializeIframeStep initializeIframeStep;
    private NemIdLoginPageStep loginPageStep;
    private NemIdVerifyLoginResponseStep verifyLoginResponseStep;
    private NemIdWaitForCodeAppResponseStep waitForCodeAppResponseStep;
    private NemIdCollectTokenStep collectTokenStep;

    private NemIdIFrameController nemIdIFrameController;

    private Credentials credentials;
    private InOrder mocksToVerifyInOrder;

    @Before
    public void setup() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);
        initializeIframeStep = mock(NemIdInitializeIframeStep.class);
        loginPageStep = mock(NemIdLoginPageStep.class);
        verifyLoginResponseStep = mock(NemIdVerifyLoginResponseStep.class);
        waitForCodeAppResponseStep = mock(NemIdWaitForCodeAppResponseStep.class);
        collectTokenStep = mock(NemIdCollectTokenStep.class);

        nemIdIFrameController =
                new NemIdIFrameController(
                        driverWrapper,
                        nemIdMetricsMock(),
                        initializeIframeStep,
                        loginPageStep,
                        verifyLoginResponseStep,
                        waitForCodeAppResponseStep,
                        collectTokenStep);

        credentials = mock(Credentials.class);
        mocksToVerifyInOrder =
                inOrder(
                        driverWrapper,
                        initializeIframeStep,
                        loginPageStep,
                        verifyLoginResponseStep,
                        waitForCodeAppResponseStep,
                        collectTokenStep);
    }

    @Test
    public void should_execute_all_steps_in_order_and_close_web_driver() {
        // when
        nemIdIFrameController.doLoginWith(credentials);

        // then
        mocksToVerifyInOrder.verify(initializeIframeStep).initializeNemIdIframe(credentials);
        mocksToVerifyInOrder.verify(loginPageStep).login(credentials);
        mocksToVerifyInOrder.verify(verifyLoginResponseStep).validateLoginResponse(credentials);
        mocksToVerifyInOrder
                .verify(waitForCodeAppResponseStep)
                .sendCodeAppRequestAndWaitForResponse(credentials);
        mocksToVerifyInOrder.verify(collectTokenStep).collectToken();

        mocksToVerifyInOrder.verify(driverWrapper).quitDriver();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }
}
