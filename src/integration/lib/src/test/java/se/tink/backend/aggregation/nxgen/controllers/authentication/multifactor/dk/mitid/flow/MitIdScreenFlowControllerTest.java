package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow;

import static java.util.Arrays.asList;
import static java.util.Collections.nCopies;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.WaitTime.WAIT_FOR_FIRST_AUTHENTICATION_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.WaitTime.WAIT_TO_GIVE_CPR_SCREEN_TIME_TO_EXIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.stubbing.answers.ReturnsElementsOf;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.mitid.MitIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreenQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.MitId2FAStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.MitIdEnterCprStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.MitIdUserIdStep;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.proxy.ProxySaveResponseFilter;

@RunWith(JUnitParamsRunner.class)
public class MitIdScreenFlowControllerTest {

    private static final RuntimeException AUTH_FINISHED_ABNORMALLY =
            new RuntimeException("Auth finished");
    private ReturnsElementsOfValues ifProxyHasResponseValues;
    private ReturnsElementsOfValues ifAuthFinishedAbnormallyValues;
    private ReturnsElementsOfValues ifCprScreenValues;

    private WebDriverService driverService;
    private MitIdScreensManager screensManager;
    private ProxySaveResponseFilter authFinishProxyFilter;
    private MitIdAuthenticator mitIdAuthenticator;

    private MitIdUserIdStep userIdStep;
    private MitId2FAStep secondFactorStep;
    private MitIdEnterCprStep enterCprStep;

    private MitIdScreenFlowController flowController;

    @Before
    public void setup() {
        driverService = mock(WebDriverService.class);
        screensManager = mock(MitIdScreensManager.class);
        MitIdProxyFiltersRegistry proxyFiltersRegistry = mock(MitIdProxyFiltersRegistry.class);
        authFinishProxyFilter = mock(ProxySaveResponseFilter.class);
        mitIdAuthenticator = mock(MitIdAuthenticator.class);
        doThrow(AUTH_FINISHED_ABNORMALLY)
                .when(mitIdAuthenticator)
                .handleAuthenticationFinishedWithAgentSpecificError(driverService);

        userIdStep = mock(MitIdUserIdStep.class);
        secondFactorStep = mock(MitId2FAStep.class);
        enterCprStep = mock(MitIdEnterCprStep.class);

        flowController =
                new MitIdScreenFlowController(
                        driverService,
                        screensManager,
                        proxyFiltersRegistry,
                        authFinishProxyFilter,
                        mitIdAuthenticator,
                        userIdStep,
                        secondFactorStep,
                        enterCprStep);

        ifProxyHasResponseValues = new ReturnsElementsOfValues();
        ifAuthFinishedAbnormallyValues = new ReturnsElementsOfValues();
        ifCprScreenValues = new ReturnsElementsOfValues();
    }

    @Test
    @Parameters(method = "paramsForShouldNotEnterUserId")
    public void should_not_enter_user_id_if_first_screen_is_2FA_screen(
            MitIdScreen secondFactorScreen) {
        // given
        mockFirstScreen(secondFactorScreen);

        mockIfProxyHasResponse(false, false, true);
        mockIfCprScreen(false, false, false);

        // when
        flowController.runScreenFlow();

        // then
        verifyNoInteractions(userIdStep);
        verify(secondFactorStep).perform2FA();
        verifyNoInteractions(enterCprStep);
    }

    @SuppressWarnings("unused")
    private static Object[] paramsForShouldNotEnterUserId() {
        return MitIdScreen.SECOND_FACTOR_SCREENS.toArray();
    }

    @Test
    public void should_not_enter_cpr_if_authentication_is_already_finished_after_2FA() {
        // given
        mockFirstScreen(MitIdScreen.USER_ID_SCREEN);

        mockIfProxyHasResponse(false, false, true);
        mockIfAuthFinishedAbnormally(false, false);
        mockIfCprScreen(false, false);

        // when
        flowController.runScreenFlow();

        // then
        verify(userIdStep).enterUserId();
        verify(secondFactorStep).perform2FA();
        verifyNoInteractions(enterCprStep);
    }

    @Test
    public void should_not_enter_cpr_if_authentication_has_finished_abnormally_after_2FA() {
        // given
        mockFirstScreen(MitIdScreen.USER_ID_SCREEN);

        mockIfProxyHasResponse(false, false, false);
        mockIfAuthFinishedAbnormally(false, false, true);
        mockIfCprScreen(false, false);

        // when
        Throwable throwable = catchThrowable(() -> flowController.runScreenFlow());

        // then
        assertThat(throwable).isEqualTo(AUTH_FINISHED_ABNORMALLY);

        verify(userIdStep).enterUserId();
        verify(secondFactorStep).perform2FA();
        verifyNoInteractions(enterCprStep);
    }

    @Test
    public void should_enter_cpr_screen_and_detect_authentication_finished_normally() {
        // given
        mockFirstScreen(MitIdScreen.USER_ID_SCREEN);

        mockIfProxyHasResponse(false, false, false);
        mockIfAuthFinishedAbnormally(false, false, false);
        mockIfCprScreen(false, false, true);

        mockIfProxyHasResponse(false, true);
        mockIfAuthFinishedAbnormally(false);

        // when
        flowController.runScreenFlow();

        // then
        verify(userIdStep).enterUserId();
        verify(secondFactorStep).perform2FA();
        verify(enterCprStep).enterCpr();
    }

    @Test
    public void should_enter_cpr_screen_and_detect_authentication_finished_abnormally() {
        // given
        mockFirstScreen(MitIdScreen.USER_ID_SCREEN);

        mockIfProxyHasResponse(false, false, false);
        mockIfAuthFinishedAbnormally(false, false, false);
        mockIfCprScreen(false, false, true);

        mockIfProxyHasResponse(false, false);
        mockIfAuthFinishedAbnormally(false, true);

        // when
        Throwable throwable = catchThrowable(() -> flowController.runScreenFlow());

        // then
        assertThat(throwable).isEqualTo(AUTH_FINISHED_ABNORMALLY);

        verify(userIdStep).enterUserId();
        verify(secondFactorStep).perform2FA();
        verify(enterCprStep).enterCpr();
    }

    @Test
    public void should_enter_cpr_screen_and_throw_invalid_cpr() {
        // given
        mockFirstScreen(MitIdScreen.USER_ID_SCREEN);

        mockIfProxyHasResponse(false, false, false, false);
        mockIfCprScreen(false, false, false, true);

        mockIfProxyHasResponse(nCopies(WAIT_TO_GIVE_CPR_SCREEN_TIME_TO_EXIT, false));
        mockIfAuthFinishedAbnormally(nCopies(WAIT_TO_GIVE_CPR_SCREEN_TIME_TO_EXIT, false));

        mockIfProxyHasResponse(false);
        mockIfAuthFinishedAbnormally(false);
        mockIfCprScreen(true);

        // when
        Throwable throwable = catchThrowable(() -> flowController.runScreenFlow());

        // then
        assertAgentError(throwable, MitIdError.INVALID_CPR);

        verify(userIdStep).enterUserId();
        verify(secondFactorStep).perform2FA();
        verify(enterCprStep).enterCpr();
    }

    @Getter
    private static class ReturnsElementsOfValues {
        private final List<Boolean> allValues = new ArrayList<>();

        private void add(Boolean... values) {
            allValues.addAll(new ArrayList<>(asList(values)));
        }

        private void add(List<Boolean> values) {
            allValues.addAll(values);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void assertAgentError(Throwable throwable, AgentError expectedError) {
        assertThat(throwable).isInstanceOf(AgentException.class);

        AgentException exception = (AgentException) throwable;
        AgentError error = exception.getError();
        assertThat(error).isEqualTo(expectedError);
    }

    @SuppressWarnings("SameParameterValue")
    private void mockFirstScreen(MitIdScreen screen) {
        when(screensManager.searchForFirstScreen(
                        MitIdScreenQuery.builder()
                                .searchForExpectedScreens(MitIdScreen.USER_ID_SCREEN)
                                .searchForExpectedScreens(MitIdScreen.SECOND_FACTOR_SCREENS)
                                .searchForSeconds(WAIT_FOR_FIRST_AUTHENTICATION_SCREEN)
                                .build()))
                .thenReturn(screen);
    }

    private void mockIfProxyHasResponse(Boolean... values) {
        mockIfProxyHasResponse(asList(values));
    }

    private void mockIfProxyHasResponse(List<Boolean> values) {
        ifProxyHasResponseValues.add(values);
        when(authFinishProxyFilter.hasResponse())
                .thenAnswer(new ReturnsElementsOf(ifProxyHasResponseValues.getAllValues()));
    }

    private void mockIfAuthFinishedAbnormally(Boolean... values) {
        mockIfAuthFinishedAbnormally(asList(values));
    }

    private void mockIfAuthFinishedAbnormally(List<Boolean> values) {
        ifAuthFinishedAbnormallyValues.add(values);
        when(mitIdAuthenticator.isAuthenticationFinishedWithAgentSpecificError(driverService))
                .thenAnswer(new ReturnsElementsOf(ifAuthFinishedAbnormallyValues.getAllValues()));
    }

    private void mockIfCprScreen(Boolean... values) {
        ifCprScreenValues.add(values);
        when(screensManager.trySearchForFirstScreen(
                        MitIdScreenQuery.builder()
                                .searchForExpectedScreens(MitIdScreen.CPR_SCREEN)
                                .searchOnlyOnce()
                                .build()))
                .thenAnswer(
                        new ReturnsElementsOf(
                                ifCprScreenValues.getAllValues().stream()
                                        .map(
                                                booleanValue -> {
                                                    if (booleanValue) {
                                                        return Optional.of(MitIdScreen.CPR_SCREEN);
                                                    } else {
                                                        return Optional.empty();
                                                    }
                                                })
                                        .collect(Collectors.toList())));
    }
}
