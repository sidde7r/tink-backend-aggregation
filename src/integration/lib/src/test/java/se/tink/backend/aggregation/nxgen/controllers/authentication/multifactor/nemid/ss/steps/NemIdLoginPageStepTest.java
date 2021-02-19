package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.PASSWORD_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.SUBMIT_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.USERNAME_INPUT;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;

public class NemIdLoginPageStepTest {

    private static final String SAMPLE_USERNAME = "--- SAMPLE USERNAME ---";
    private static final String SAMPLE_PASSWORD = "--- SAMPLE PASSWORD ---";

    private NemIdWebDriverWrapper driverWrapper;
    private NemIdCredentialsStatusUpdater statusUpdater;

    private NemIdLoginPageStep loginPageStep;

    private InOrder mocksToVerifyInOrder;

    @Before
    public void setup() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);
        statusUpdater = mock(NemIdCredentialsStatusUpdater.class);

        loginPageStep = new NemIdLoginPageStep(driverWrapper, statusUpdater);

        mocksToVerifyInOrder = inOrder(driverWrapper, statusUpdater);
    }

    @Test
    public void should_enter_credentials_then_click_login_and_update_status_payload() {
        // given
        Credentials credentials = mock(Credentials.class);
        when(credentials.getField(Field.Key.USERNAME)).thenReturn(SAMPLE_USERNAME);
        when(credentials.getField(Field.Key.PASSWORD)).thenReturn(SAMPLE_PASSWORD);

        // when
        loginPageStep.login(credentials);

        // then
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .setValueToElement(SAMPLE_USERNAME, USERNAME_INPUT);
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .setValueToElement(SAMPLE_PASSWORD, PASSWORD_INPUT);
        mocksToVerifyInOrder.verify(driverWrapper).clickButton(SUBMIT_BUTTON);

        mocksToVerifyInOrder
                .verify(statusUpdater)
                .updateStatusPayload(credentials, UserMessage.VERIFYING_CREDS);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }
}
