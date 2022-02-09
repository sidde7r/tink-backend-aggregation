package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.codeapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.Timeouts.CODE_APP_POLLING_RESULT_TIMEOUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.Timeouts.CODE_APP_SCREEN_SEARCH_TIMEOUT;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.exceptions.mitid.MitIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.fields.MitIdCodeAppField;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreenQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.Catalog;

@RunWith(JUnitParamsRunner.class)
public class MitIdCodeAppStepTest {

    private SupplementalInformationController supplementalInformationController;
    private Catalog catalog;
    private MitIdScreensManager screensManager;
    private MitIdCodeAppPollingProxyFilter pollingProxyListener;

    private MitIdCodeAppStep codeAppStep;

    @Before
    public void setup() {
        supplementalInformationController = mock(SupplementalInformationController.class);
        catalog = mock(Catalog.class);
        screensManager = mock(MitIdScreensManager.class);
        pollingProxyListener = mock(MitIdCodeAppPollingProxyFilter.class);

        codeAppStep =
                new MitIdCodeAppStep(
                        supplementalInformationController,
                        catalog,
                        screensManager,
                        pollingProxyListener);
    }

    @Test
    public void should_authenticate_successfully_with_whatever_supplemental_info_Response() {
        // given
        mockIsOnCodeAppScreen();
        mockNotEmptySupplementalInfoResponse();
        mockPollingResult(MitIdCodeAppPollingResult.OK);

        // when
        codeAppStep.authenticateWithCodeApp();

        // then
        verifySearchesForCodeAppScreen();
        verifyDisplaysAppPrompt();
        verifyPollsAppResult();
    }

    @Test
    public void should_authenticate_successfully_with_empty_supplemental_info_Response() {
        // given
        mockIsOnCodeAppScreen();
        mockEmptySupplementalInfoResponse();
        mockPollingResult(MitIdCodeAppPollingResult.OK);

        // when
        codeAppStep.authenticateWithCodeApp();

        // then
        verifySearchesForCodeAppScreen();
        verifyDisplaysAppPrompt();
        verifyPollsAppResult();
    }

    @Test
    @Parameters(method = "paramsForShouldThrowCorrectException")
    public void should_throw_correct_exception_on_not_ok_polling_result(
            MitIdCodeAppPollingResult pollingResult, Exception expectedException) {
        // given
        mockIsOnCodeAppScreen();
        mockNotEmptySupplementalInfoResponse();
        mockPollingResult(pollingResult);

        // when
        Throwable throwable = catchThrowable(() -> codeAppStep.authenticateWithCodeApp());

        // then
        assertThat(throwable).usingRecursiveComparison().isEqualTo(expectedException);

        verifySearchesForCodeAppScreen();
        verifyDisplaysAppPrompt();
        verifyPollsAppResult();
    }

    @SuppressWarnings("unused")
    private static Object[] paramsForShouldThrowCorrectException() {
        Map<MitIdCodeAppPollingResult, Exception> errorMap =
                ImmutableMap.of(
                        MitIdCodeAppPollingResult.EXPIRED, MitIdError.CODE_APP_TIMEOUT.exception(),
                        MitIdCodeAppPollingResult.REJECTED,
                                MitIdError.CODE_APP_REJECTED.exception(),
                        MitIdCodeAppPollingResult.TECHNICAL_ERROR,
                                MitIdError.CODE_APP_TECHNICAL_ERROR.exception(),
                        MitIdCodeAppPollingResult.UNKNOWN,
                                new IllegalStateException(
                                        "Unknown MitID code app polling result."));

        return errorMap.entrySet().stream()
                .map(entry -> new Object[] {entry.getKey(), entry.getValue()})
                .toArray();
    }

    @Test
    public void should_throw_on_missing_code_app_listener_response() {
        // given
        mockIsOnCodeAppScreen();
        mockNotEmptySupplementalInfoResponse();
        mockPollingResult(null);

        // when
        Throwable throwable = catchThrowable(() -> codeAppStep.authenticateWithCodeApp());

        // then
        assertThat(throwable)
                .usingRecursiveComparison()
                .isEqualTo(new IllegalStateException("No MitID code app polling response"));

        verifySearchesForCodeAppScreen();
        verifyDisplaysAppPrompt();
        verifyPollsAppResult();
    }

    private void mockPollingResult(@Nullable MitIdCodeAppPollingResult pollingResult) {
        when(pollingProxyListener.waitForResult(CODE_APP_POLLING_RESULT_TIMEOUT))
                .thenReturn(Optional.ofNullable(pollingResult));
    }

    private void verifyPollsAppResult() {
        verify(pollingProxyListener).waitForResult(CODE_APP_POLLING_RESULT_TIMEOUT);
    }

    private void mockIsOnCodeAppScreen() {
        when(screensManager.searchForFirstScreen(any())).thenReturn(MitIdScreen.CODE_APP_SCREEN);
    }

    private void verifySearchesForCodeAppScreen() {
        verify(screensManager)
                .searchForFirstScreen(
                        MitIdScreenQuery.builder()
                                .searchForExpectedScreens(MitIdScreen.CODE_APP_SCREEN)
                                .searchForSeconds(CODE_APP_SCREEN_SEARCH_TIMEOUT)
                                .build());
    }

    private void mockNotEmptySupplementalInfoResponse() {
        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenReturn(ImmutableMap.of("whateverKey", "whateverValue"));
    }

    private void mockEmptySupplementalInfoResponse() {
        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenThrow(SupplementalInfoError.NO_VALID_CODE.exception());
    }

    private void verifyDisplaysAppPrompt() {
        verify(supplementalInformationController)
                .askSupplementalInformationSync(
                        MitIdCodeAppField.build(catalog).toArray(new Field[0]));
    }
}
