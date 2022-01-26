package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.BANK_SIDE_FAILURE;
import static se.tink.backend.aggregation.agents.exceptions.errors.SessionError.SESSION_EXPIRED;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(MockitoJUnitRunner.class)
public class OpenIdAccessTokenRefresherTest {
    @Mock private OpenIdApiClient openIdApiClient;
    @Mock private Credentials credentials;
    private ListAppender<ILoggingEvent> listAppender;
    private OpenIdAccessTokenRefresher tokenRefresher;

    @Before
    public void setUp() throws Exception {
        Logger log = (Logger) LoggerFactory.getLogger(OpenIdAccessTokenRefresher.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        log.addAppender(listAppender);
        tokenRefresher = new OpenIdAccessTokenRefresher(openIdApiClient, credentials);
    }

    @Test
    public void shouldThrowSessionExpiredExceptionWhenRefreshedTokenIsNotPresent() {
        // given
        OAuth2Token oAuth2Token = getSampleOAuth2TokenWithoutRefreshToken();
        // when
        // then
        assertThatThrownBy(() -> tokenRefresher.refresh(oAuth2Token))
                .isInstanceOfSatisfying(
                        SessionException.class,
                        e -> Assertions.assertThat(e.getError()).isEqualTo(SESSION_EXPIRED));

        List<String> logs =
                listAppender.list.stream()
                        .map(ILoggingEvent::getFormattedMessage)
                        .collect(Collectors.toList());
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0))
                .containsSubsequence(
                        "[OpenIdAccessTokenRefresher] Trying to refresh access token.");
        assertThat(logs.get(1))
                .containsSubsequence(
                        "[OpenIdAccessTokenRefresher] Refresh token is not present. "
                                + "Access token refresh failed.");
    }

    @Test
    public void shouldThrowSessionExpiredExceptionRefreshedTokenIsInvalid() {
        // given
        OAuth2Token oAuth2Token = getSampleOAuth2Token();
        OAuth2Token invalidToken = getSampleInvalidOAuth2Token();
        when(openIdApiClient.refreshAccessToken(anyString(), any(ClientMode.class)))
                .thenReturn(invalidToken);
        // when
        // then
        assertThatThrownBy(() -> tokenRefresher.refresh(oAuth2Token))
                .isInstanceOfSatisfying(
                        SessionException.class,
                        e -> Assertions.assertThat(e.getError()).isEqualTo(SESSION_EXPIRED));

        List<String> logs =
                listAppender.list.stream()
                        .map(ILoggingEvent::getFormattedMessage)
                        .collect(Collectors.toList());
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0))
                .containsSubsequence(
                        "[OpenIdAccessTokenRefresher] Trying to refresh access token.");
        assertThat(logs.get(1))
                .containsSubsequence(
                        "[OpenIdAccessTokenRefresher] Access token refreshed, but it is invalid.");
    }

    @Test
    public void shouldThrowBankServiceExceptionWhenReceivedResponseExceptionWithStatusAbove500() {
        // given
        OAuth2Token oAuth2Token = getSampleOAuth2Token();
        HttpResponseException exception = mock(HttpResponseException.class);
        when(openIdApiClient.refreshAccessToken(anyString(), any(ClientMode.class)))
                .thenThrow(exception);
        HttpResponse response = mock(HttpResponse.class);
        when(exception.getResponse()).thenReturn(response);
        when(response.getStatus()).thenReturn(503);
        // when
        // then
        assertThatThrownBy(() -> tokenRefresher.refresh(oAuth2Token))
                .isInstanceOfSatisfying(
                        BankServiceException.class,
                        e -> Assertions.assertThat(e.getError()).isEqualTo(BANK_SIDE_FAILURE));

        List<String> logs =
                listAppender.list.stream()
                        .map(ILoggingEvent::getFormattedMessage)
                        .collect(Collectors.toList());
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0))
                .containsSubsequence(
                        "[OpenIdAccessTokenRefresher] Trying to refresh access token.");
        assertThat(logs.get(1)).containsSubsequence("[OpenIdAccessTokenRefresher] Bank side error");
    }

    @Test
    public void
            shouldThrowSessionExpiredExceptionWhenReceivedResponseExceptionWithStatusBelow500() {
        // given
        OAuth2Token oAuth2Token = getSampleOAuth2Token();
        HttpResponseException exception = mock(HttpResponseException.class);
        when(openIdApiClient.refreshAccessToken(anyString(), any(ClientMode.class)))
                .thenThrow(exception);
        HttpResponse response = mock(HttpResponse.class);
        when(exception.getResponse()).thenReturn(response);
        when(response.getStatus()).thenReturn(418);
        // when
        // then
        assertThatThrownBy(() -> tokenRefresher.refresh(oAuth2Token))
                .isInstanceOfSatisfying(
                        SessionException.class,
                        e -> Assertions.assertThat(e.getError()).isEqualTo(SESSION_EXPIRED));

        List<String> logs =
                listAppender.list.stream()
                        .map(ILoggingEvent::getFormattedMessage)
                        .collect(Collectors.toList());
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0))
                .containsSubsequence(
                        "[OpenIdAccessTokenRefresher] Trying to refresh access token.");
        assertThat(logs.get(1))
                .containsSubsequence("[OpenIdAccessTokenRefresher] Access token refresh failed:");
    }

    private OAuth2Token getSampleOAuth2TokenWithoutRefreshToken() {
        return OAuth2Token.createBearer("dummyAccessToken", null, 1000);
    }

    private OAuth2Token getSampleOAuth2Token() {
        return OAuth2Token.createBearer("dummyAccessToken", "dummyRefreshToken", 1000);
    }

    private OAuth2Token getSampleInvalidOAuth2Token() {
        return OAuth2Token.createBearer("", "dummyRefreshToken", 1000);
    }
}
