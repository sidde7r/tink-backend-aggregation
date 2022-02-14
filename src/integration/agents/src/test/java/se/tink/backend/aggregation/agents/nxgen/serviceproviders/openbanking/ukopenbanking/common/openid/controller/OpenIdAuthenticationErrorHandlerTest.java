package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage.data.ConsentDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdApiClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(JUnitParamsRunner.class)
public class OpenIdAuthenticationErrorHandlerTest {

    private OpenIdAuthenticationErrorHandler errorHandler;
    private ListAppender<ILoggingEvent> listAppender;

    @Before
    public void setUp() throws Exception {
        Logger log = (Logger) LoggerFactory.getLogger(OpenIdAuthenticationErrorHandler.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        log.addAppender(listAppender);
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        ConsentDataStorage consentDataStorage = new ConsentDataStorage(persistentStorage);
        errorHandler =
                new OpenIdAuthenticationErrorHandler(
                        consentDataStorage, mock(OpenIdApiClient.class));
    }

    @Test
    public void shouldOnlyLogSuccessMessageWhenCallbackDataDoesNotContainAnyError() {
        // given
        // when
        errorHandler.handle(new HashMap<>());
        // then
        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage, ILoggingEvent::getLevel)
                .containsExactly(
                        Tuple.tuple(
                                "[OpenIdAuthenticationErrorHandler] OpenId callback success.",
                                Level.INFO));
    }

    @Test
    @Parameters
    public void shouldThrowIncorrectCredentialsException(Map<String, String> callbackData) {
        // given
        // when
        ThrowingCallable throwingCallable = () -> errorHandler.handle(callbackData);
        // then
        assertThatThrownBy(throwingCallable)
                .isInstanceOfSatisfying(
                        LoginException.class,
                        e -> assertThat(e.getError()).isEqualTo(LoginError.INCORRECT_CREDENTIALS));
    }

    public Object[] parametersForShouldThrowIncorrectCredentialsException() {
        return new Object[] {
            ImmutableMap.of("error", "access_denied"), ImmutableMap.of("error", "login_required")
        };
    }

    @Test
    @Parameters
    public void shouldThrowException(
            Map<String, String> providedCallbackData,
            Class<? extends AgentException> expectedExceptionClass,
            AgentError expectedError,
            String expectedMessage) {
        // given
        // when
        ThrowingCallable throwingCallable = () -> errorHandler.handle(providedCallbackData);
        // then
        assertThatThrownBy(throwingCallable)
                .isInstanceOfSatisfying(
                        expectedExceptionClass,
                        e -> assertThat(e.getError()).isEqualTo(expectedError))
                .hasMessage(expectedMessage);
    }

    public Object[] parametersForShouldThrowException() {
        return new Object[] {
            new Object[] {
                ImmutableMap.of("error", "server_error"),
                BankServiceException.class,
                BankServiceError.BANK_SIDE_FAILURE,
                ""
            },
            new Object[] {
                ImmutableMap.of("error", "temporarily_unavailable"),
                BankServiceException.class,
                BankServiceError.NO_BANK_SERVICE,
                ""
            },
            new Object[] {
                ImmutableMap.of("error", "401 Unauthorised"),
                BankServiceException.class,
                BankServiceError.SESSION_TERMINATED,
                ""
            },
            new Object[] {
                ImmutableMap.<String, String>builder()
                        .put("error", "server_error")
                        .put("error_description", "server_error_processing")
                        .build(),
                ThirdPartyAppException.class,
                ThirdPartyAppError.CANCELLED,
                "server_error_processing"
            },
            new Object[] {
                ImmutableMap.of("error", "invalid_openbanking_intent_id"),
                SessionException.class,
                SessionError.CONSENT_INVALID,
                "Cause: SessionError.CONSENT_INVALID"
            }
        };
    }

    @Test
    public void shouldThrowDefaultExceptionWhenUnknownErrorOccurs() {
        // given
        Map<String, String> callbackData = ImmutableMap.of("error", "dummyError");
        // when
        ThrowingCallable throwingCallable = () -> errorHandler.handle(callbackData);
        // then
        assertThatThrownBy(throwingCallable)
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "[OpenIdAuthenticationErrorHandler] Unknown error with details:");
    }
}
