package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage.data.ConsentDataStorage;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(MockitoJUnitRunner.class)
public class OpenIdAuthenticationErrorHandlerTest {

    @Mock private PersistentStorage persistentStorage;
    @Mock private OpenIdApiClient openIdApiClient;
    private OpenIdAuthenticationErrorHandler errorHandler;
    private ListAppender<ILoggingEvent> listAppender;

    @Before
    public void setUp() throws Exception {
        Logger log = (Logger) LoggerFactory.getLogger(OpenIdAuthenticationErrorHandler.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        log.addAppender(listAppender);
        ConsentDataStorage consentDataStorage = new ConsentDataStorage(persistentStorage);
        errorHandler = new OpenIdAuthenticationErrorHandler(consentDataStorage, openIdApiClient);
    }

    @Test
    public void shouldNotThrowAnyException() {
        // given
        errorHandler.handle(new HashMap<>());
        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage, ILoggingEvent::getLevel)
                .containsExactly(
                        Tuple.tuple(
                                "[OpenIdAuthenticationErrorHandler] OpenId callback success.",
                                Level.INFO));
    }

    @Test
    public void shouldThrowIncorrectCredentialsExceptionWhenAccessDenied() {
        // given
        Map<String, String> callbackData = ImmutableMap.of("error", "access_denied");
        // when
        // then
        assertThatThrownBy(() -> errorHandler.handle(callbackData))
                .isInstanceOfSatisfying(
                        LoginException.class,
                        e -> assertThat(e.getError()).isEqualTo(LoginError.INCORRECT_CREDENTIALS));
        verify(openIdApiClient, times(1)).storeOpenIdError(any(ErrorEntity.class));
    }

    @Test
    public void shouldThrowIncorrectCredentialsExceptionWhenLoginIsNotProvided() {
        // given
        Map<String, String> callbackData = ImmutableMap.of("error", "login_required");
        // when
        // then
        assertThatThrownBy(() -> errorHandler.handle(callbackData))
                .isInstanceOfSatisfying(
                        LoginException.class,
                        e -> assertThat(e.getError()).isEqualTo(LoginError.INCORRECT_CREDENTIALS));
        verify(openIdApiClient, times(1)).storeOpenIdError(any(ErrorEntity.class));
    }

    @Test
    public void shouldThrowThirdPartyErrorCancelledWhenServerErrorOccursAndDescriptionIsNotEmpty() {
        // given
        Map<String, String> callbackData =
                ImmutableMap.<String, String>builder()
                        .put("error", "server_error")
                        .put("error_description", "server_error_processing")
                        .build();
        // when
        // then
        assertThatThrownBy(() -> errorHandler.handle(callbackData))
                .isInstanceOfSatisfying(
                        ThirdPartyAppException.class,
                        e -> assertThat(e.getError()).isEqualTo(ThirdPartyAppError.CANCELLED))
                .hasMessage("server_error_processing");
    }

    @Test
    public void shouldThrowBankSideFailureExceptionWhenServerErrorOccursAndDescriptionIsEmpty() {
        // given
        Map<String, String> callbackData = ImmutableMap.of("error", "server_error");
        // when
        // then
        assertThatThrownBy(() -> errorHandler.handle(callbackData))
                .isInstanceOfSatisfying(
                        BankServiceException.class,
                        e -> assertThat(e.getError()).isEqualTo(BankServiceError.BANK_SIDE_FAILURE))
                .hasMessage("");
    }

    @Test
    public void shouldThrowNoBankServiceExceptionWhenServiceTemporarilyUnavailable() {
        // given
        Map<String, String> callbackData = ImmutableMap.of("error", "temporarily_unavailable");
        // when
        // then
        assertThatThrownBy(() -> errorHandler.handle(callbackData))
                .isInstanceOfSatisfying(
                        BankServiceException.class,
                        e -> assertThat(e.getError()).isEqualTo(BankServiceError.NO_BANK_SERVICE))
                .hasMessage("");
    }

    @Test
    public void shouldThrowSessionTerminatedExceptionWhenUnauthorisedRequest() {
        // given
        Map<String, String> callbackData = ImmutableMap.of("error", "401 Unauthorised");
        // when
        // then
        assertThatThrownBy(() -> errorHandler.handle(callbackData))
                .isInstanceOfSatisfying(
                        BankServiceException.class,
                        e ->
                                assertThat(e.getError())
                                        .isEqualTo(BankServiceError.SESSION_TERMINATED))
                .hasMessage("");
    }

    @Test
    public void shouldThrowConsentInvalidExceptionWhenInvalidIntentIdProvided() {
        // given
        Map<String, String> callbackData =
                ImmutableMap.of("error", "invalid_openbanking_intent_id");
        // when
        // then
        assertThatThrownBy(() -> errorHandler.handle(callbackData))
                .isInstanceOfSatisfying(
                        SessionException.class,
                        e -> assertThat(e.getError()).isEqualTo(SessionError.CONSENT_INVALID));
    }

    @Test
    public void shouldThrowDefaultExceptionWhenUnknownErrorOccurs() {
        // given
        Map<String, String> callbackData = ImmutableMap.of("error", "dummyError");
        // when
        // then
        assertThatThrownBy(() -> errorHandler.handle(callbackData))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "[OpenIdAuthenticationErrorHandler] Unknown error with details:");
    }
}
