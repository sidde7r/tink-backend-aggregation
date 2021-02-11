package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_APP_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.nemIdMetricsMock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

@RequiredArgsConstructor
public class NemIdWaitForCodeAppResponseStepTest {

    private NemIdWebDriverWrapper driverWrapper;
    private NemIdCredentialsStatusUpdater statusUpdater;
    private Catalog catalog;
    private SupplementalInformationController supplementalInformationController;

    private NemIdWaitForCodeAppResponseStep waitForCodeAppResponseStep;

    private Credentials credentials;
    private InOrder mocksToVerifyInOrder;

    @Before
    public void setup() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);
        statusUpdater = mock(NemIdCredentialsStatusUpdater.class);
        catalog = mock(Catalog.class);
        supplementalInformationController = mock(SupplementalInformationController.class);

        waitForCodeAppResponseStep =
                new NemIdWaitForCodeAppResponseStep(
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
    public void
            should_click_code_app_method_button_then_update_status_payload_and_wait_for_user_response() {
        // given
        mockSupplementalInfoResponse(true);

        // when
        waitForCodeAppResponseStep.sendCodeAppRequestAndWaitForResponse(credentials);

        // then
        mocksToVerifyInOrder.verify(driverWrapper).clickButton(NEMID_CODE_APP_BUTTON);
        mocksToVerifyInOrder
                .verify(statusUpdater)
                .updateStatusPayload(credentials, UserMessage.OPEN_NEM_ID_APP_AND_CLICK_BUTTON);
        String mfaId =
                mocksToVerifyInOrder
                        .verify(supplementalInformationController)
                        .askSupplementalInformationAsync(any(Field.class));
        mocksToVerifyInOrder
                .verify(supplementalInformationController)
                .waitForSupplementalInformation(
                        mfaId,
                        NemIdConstants.NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN,
                        TimeUnit.SECONDS,
                        true);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void
            should_click_code_app_method_button_then_update_status_payload_and_throw_error_on_user_response_timeout() {
        // given
        mockSupplementalInfoResponse(false);

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                waitForCodeAppResponseStep.sendCodeAppRequestAndWaitForResponse(
                                        credentials));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, SupplementalInfoError.WAIT_TIMEOUT.exception());

        mocksToVerifyInOrder.verify(driverWrapper).clickButton(NEMID_CODE_APP_BUTTON);
        mocksToVerifyInOrder
                .verify(statusUpdater)
                .updateStatusPayload(credentials, UserMessage.OPEN_NEM_ID_APP_AND_CLICK_BUTTON);
        String mfaId =
                mocksToVerifyInOrder
                        .verify(supplementalInformationController)
                        .askSupplementalInformationAsync(any(Field.class));
        mocksToVerifyInOrder
                .verify(supplementalInformationController)
                .waitForSupplementalInformation(
                        mfaId,
                        NemIdConstants.NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN,
                        TimeUnit.SECONDS,
                        true);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    private void mockSupplementalInfoResponse(boolean withResponse) {
        when(supplementalInformationController.waitForSupplementalInformation(
                        any(), anyLong(), any(), anyBoolean()))
                .thenReturn(
                        withResponse
                                ? Optional.of(new HashMap<String, String>())
                                : Optional.empty());
    }
}
