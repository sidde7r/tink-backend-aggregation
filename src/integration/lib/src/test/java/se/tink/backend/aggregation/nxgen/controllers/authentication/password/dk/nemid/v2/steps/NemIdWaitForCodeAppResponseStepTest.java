package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.NEMID_CODE_APP_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.util.NemIdTestHelper.nemIdMetricsMock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.util.NemIdTestHelper.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.utils.supplementalfields.DanishFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdWebDriverWrapper;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
public class NemIdWaitForCodeAppResponseStepTest {

    private NemIdWebDriverWrapper driverWrapper;
    private NemIdCredentialsStatusUpdater statusUpdater;
    private Catalog catalog;
    private SupplementalRequester supplementalRequester;

    private NemIdWaitForCodeAppResponseStep waitForCodeAppResponseStep;

    private Credentials credentials;
    private InOrder mocksToVerifyInOrder;

    @Before
    public void setup() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);
        statusUpdater = mock(NemIdCredentialsStatusUpdater.class);
        catalog = mock(Catalog.class);
        supplementalRequester = mock(SupplementalRequester.class);

        waitForCodeAppResponseStep =
                new NemIdWaitForCodeAppResponseStep(
                        driverWrapper,
                        nemIdMetricsMock(),
                        statusUpdater,
                        catalog,
                        supplementalRequester);

        credentials = mock(Credentials.class);
        mocksToVerifyInOrder =
                inOrder(driverWrapper, statusUpdater, credentials, supplementalRequester);
    }

    @Test
    public void
            should_click_code_app_method_button_then_update_status_payload_and_wait_for_user_response() {
        // given
        mockSupplementalInfoResponse("some not null supplemental response");

        // when
        waitForCodeAppResponseStep.sendCodeAppRequestAndWaitForResponse(credentials);

        // then
        mocksToVerifyInOrder.verify(driverWrapper).clickButton(NEMID_CODE_APP_BUTTON);
        mocksToVerifyInOrder
                .verify(statusUpdater)
                .updateStatusPayload(credentials, UserMessage.OPEN_NEM_ID_APP_AND_CLICK_BUTTON);

        mocksToVerifyInOrder
                .verify(credentials)
                .setSupplementalInformation(
                        SerializationUtils.serializeToString(
                                Collections.singletonList(DanishFields.NemIdInfo.build(catalog))));
        mocksToVerifyInOrder
                .verify(credentials)
                .setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        mocksToVerifyInOrder
                .verify(supplementalRequester)
                .requestSupplementalInformation(
                        credentials,
                        NemIdConstantsV2.NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN,
                        TimeUnit.SECONDS,
                        true);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void
            should_click_code_app_method_button_then_update_status_payload_and_throw_error_on_user_response_timeout() {
        // given
        mockSupplementalInfoResponse(null);

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

        mocksToVerifyInOrder
                .verify(credentials)
                .setSupplementalInformation(
                        SerializationUtils.serializeToString(
                                Collections.singletonList(DanishFields.NemIdInfo.build(catalog))));
        mocksToVerifyInOrder
                .verify(credentials)
                .setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        mocksToVerifyInOrder
                .verify(supplementalRequester)
                .requestSupplementalInformation(
                        credentials,
                        NemIdConstantsV2.NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN,
                        TimeUnit.SECONDS,
                        true);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    private void mockSupplementalInfoResponse(String response) {
        when(supplementalRequester.requestSupplementalInformation(
                        any(), anyLong(), any(), anyBoolean()))
                .thenReturn(response);
    }
}
