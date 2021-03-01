package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codeapp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.SUBMIT_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.nemIdMetricsMock;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.utils.supplementalfields.DanishFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

public class NemIdCodeAppAskUserToApproveRequestStepTest {

    private NemIdWebDriverWrapper driverWrapper;
    private NemIdCredentialsStatusUpdater statusUpdater;
    private Catalog catalog;
    private SupplementalInformationController supplementalInformationController;

    private NemIdCodeAppAskUserToApproveRequestStep waitForCodeAppResponseStep;

    private Credentials credentials;
    private InOrder mocksToVerifyInOrder;

    @Before
    public void setup() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);
        statusUpdater = mock(NemIdCredentialsStatusUpdater.class);
        catalog = mock(Catalog.class);
        supplementalInformationController = mock(SupplementalInformationController.class);

        waitForCodeAppResponseStep =
                new NemIdCodeAppAskUserToApproveRequestStep(
                        driverWrapper,
                        nemIdMetricsMock(),
                        statusUpdater,
                        catalog,
                        supplementalInformationController);

        credentials = mock(Credentials.class);
        mocksToVerifyInOrder =
                inOrder(
                        driverWrapper,
                        statusUpdater,
                        credentials,
                        supplementalInformationController);
    }

    @Test
    public void should_click_send_code_app_request_button_and_wait_for_not_empty_user_response() {
        // given
        mockSupplementalInfoResponse(ImmutableMap.of());

        // when
        waitForCodeAppResponseStep.sendCodeAppRequestAndWaitForResponse(credentials);

        // then
        mocksToVerifyInOrder.verify(driverWrapper).clickButton(SUBMIT_BUTTON);
        mocksToVerifyInOrder
                .verify(statusUpdater)
                .updateStatusPayload(credentials, UserMessage.OPEN_NEM_ID_APP_AND_CLICK_BUTTON);
        mocksToVerifyInOrder
                .verify(supplementalInformationController)
                .askSupplementalInformationSync(DanishFields.NemIdInfo.build(catalog));
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_click_send_code_app_request_button_and_ignore_empty_user_response() {
        // given
        mockEmptySupplementalInfoResponse();

        // when
        waitForCodeAppResponseStep.sendCodeAppRequestAndWaitForResponse(credentials);

        // then
        mocksToVerifyInOrder.verify(driverWrapper).clickButton(SUBMIT_BUTTON);
        mocksToVerifyInOrder
                .verify(statusUpdater)
                .updateStatusPayload(credentials, UserMessage.OPEN_NEM_ID_APP_AND_CLICK_BUTTON);
        mocksToVerifyInOrder
                .verify(supplementalInformationController)
                .askSupplementalInformationSync(DanishFields.NemIdInfo.build(catalog));
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    private void mockSupplementalInfoResponse(Map<String, String> response) {
        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenReturn(response);
    }

    private void mockEmptySupplementalInfoResponse() {
        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenThrow(SupplementalInfoError.NO_VALID_CODE.exception());
    }
}
