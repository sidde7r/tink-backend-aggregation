package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdFlowController;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.libraries.credentials.service.UserAvailability;

public class MitIdAuthenticationControllerTest {

    private Credentials credentials;
    private MitIdAuthenticationResult mitIdAuthenticationResult;

    private UserAvailability userAvailability;
    private WebDriverService driverService;
    private AgentTemporaryStorage agentTemporaryStorage;
    private MitIdAuthenticator authenticator;
    private MitIdFlowController flowController;
    private InOrder mocksToVerifyInOrder;

    private MitIdAuthenticationController authenticationController;

    @Before
    public void setup() {
        credentials = mock(Credentials.class);
        mitIdAuthenticationResult = mock(MitIdAuthenticationResult.class);

        userAvailability = mock(UserAvailability.class);
        driverService = mock(WebDriverService.class);
        agentTemporaryStorage = mock(AgentTemporaryStorage.class);
        authenticator = mock(MitIdAuthenticator.class);
        flowController = mock(MitIdFlowController.class);
        when(flowController.authenticate()).thenReturn(mitIdAuthenticationResult);

        mocksToVerifyInOrder = inOrder(flowController, authenticator, driverService);

        authenticationController =
                new MitIdAuthenticationController(
                        userAvailability,
                        driverService,
                        agentTemporaryStorage,
                        authenticator,
                        flowController);
    }

    @Test
    public void should_throw_session_expired_when_user_is_not_present() {
        // given
        when(userAvailability.isUserPresent()).thenReturn(false);

        // when
        Throwable throwable =
                catchThrowable(() -> authenticationController.authenticate(credentials));

        // then
        assertThat(throwable).isInstanceOf(SessionError.SESSION_EXPIRED.exception().getClass());
        mocksToVerifyInOrder.verify(driverService).terminate(agentTemporaryStorage);
    }

    @Test
    public void should_execute_correct_methods_in_order() {
        // given
        when(userAvailability.isUserPresent()).thenReturn(true);

        // when
        authenticationController.authenticate(credentials);

        // then
        mocksToVerifyInOrder.verify(flowController).registerProxyListeners();
        mocksToVerifyInOrder.verify(authenticator).initializeMitIdWindow(driverService);
        mocksToVerifyInOrder.verify(flowController).authenticate();
        mocksToVerifyInOrder.verify(authenticator).finishAuthentication(mitIdAuthenticationResult);
        mocksToVerifyInOrder.verify(driverService).terminate(agentTemporaryStorage);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_rethrow_exceptions_with_page_source_added() {
        // given
        ListAppender<ILoggingEvent> loggingEventListAppender = setupLoggingEventListAppender();

        when(userAvailability.isUserPresent()).thenReturn(true);
        when(driverService.getFullPageSourceLog(anyInt())).thenReturn("--PAGE SOURCE--");

        RuntimeException exception = new RuntimeException("some message");
        when(flowController.authenticate()).thenThrow(exception);

        // when
        Throwable throwable =
                catchThrowable(() -> authenticationController.authenticate(credentials));

        // then
        assertThat(throwable).isEqualTo(exception);

        List<String> logs = extractLoggedMessages(loggingEventListAppender);
        assertThat(logs)
                .anyMatch(
                        message ->
                                message.contains("some message")
                                        && message.contains("--PAGE SOURCE--"));
        mocksToVerifyInOrder.verify(driverService).getFullPageSourceLog(anyInt());
        mocksToVerifyInOrder.verify(driverService).terminate(agentTemporaryStorage);
    }

    private ListAppender<ILoggingEvent> setupLoggingEventListAppender() {
        Logger logbackLogger =
                (Logger) LoggerFactory.getLogger(MitIdAuthenticationController.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logbackLogger.addAppender(listAppender);
        return listAppender;
    }

    private List<String> extractLoggedMessages(
            ListAppender<ILoggingEvent> loggingEventListAppender) {
        return loggingEventListAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList());
    }
}
