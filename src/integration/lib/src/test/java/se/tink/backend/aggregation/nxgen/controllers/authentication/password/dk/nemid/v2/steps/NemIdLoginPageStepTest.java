package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.PASSWORD_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.SUBMIT_LOGIN_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.USERNAME_INPUT;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdWebDriverWrapper;

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
        mocksToVerifyInOrder.verify(driverWrapper).clickButton(SUBMIT_LOGIN_BUTTON);

        mocksToVerifyInOrder
                .verify(statusUpdater)
                .updateStatusPayload(credentials, UserMessage.VERIFYING_CREDS);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }
}
