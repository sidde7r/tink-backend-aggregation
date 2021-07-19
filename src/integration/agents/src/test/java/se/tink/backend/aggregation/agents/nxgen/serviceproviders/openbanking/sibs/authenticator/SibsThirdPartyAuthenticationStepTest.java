package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.google.common.collect.ImmutableMap;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SibsThirdPartyAuthenticationStepTest {

    private ConsentManager consentManager;
    private StrongAuthenticationState strongAuthenticationState;
    private SibsAuthenticator authenticator;
    private SibsThirdPartyAppRequestParamsProvider objectUnderTest;
    private Map<String, String> callbackData = new HashMap<>();
    private AuthenticationRequest authenticationRequest;
    private LocalDateTimeSource localDateTimeSource;
    private SibsRetryTimeConfiguration sibsRetryTimeConfiguration;

    @Before
    public void init() throws MalformedURLException {
        consentManager = Mockito.mock(ConsentManager.class);
        Mockito.when(consentManager.create()).thenReturn(new URL("http://127.0.0.1"));
        strongAuthenticationState = Mockito.mock(StrongAuthenticationState.class);
        authenticator = Mockito.mock(SibsAuthenticator.class);
        localDateTimeSource = Mockito.mock(LocalDateTimeSource.class);
        sibsRetryTimeConfiguration = new SibsRetryTimeConfiguration(0, 1L);
        objectUnderTest =
                new SibsThirdPartyAppRequestParamsProvider(
                        consentManager,
                        authenticator,
                        strongAuthenticationState,
                        localDateTimeSource,
                        sibsRetryTimeConfiguration);
        callbackData.put("key", "value");
        authenticationRequest = new AuthenticationRequest(Mockito.mock(Credentials.class));
    }

    @Test
    public void shouldHandleSuccessAuthentication()
            throws AuthenticationException, AuthorizationException {
        // given
        Mockito.when(consentManager.getStatus()).thenReturn(ConsentStatus.ACTC);
        mockTime();
        authenticationRequest.withCallbackData(ImmutableMap.copyOf(callbackData));
        // when
        objectUnderTest.processThirdPartyCallback(new HashMap<>());
        // then
        Mockito.verify(authenticator).handleManualAuthenticationSuccess();
    }

    @Test(expected = AuthorizationException.class)
    public void shouldHandleFailedAuthenticationWhenConsentStatusIsNotAccepted()
            throws AuthenticationException, AuthorizationException {
        // given
        Mockito.when(consentManager.getStatus()).thenReturn(ConsentStatus.RJCT);
        mockTime(
                LocalDateTime.now().minusHours(1L),
                LocalDateTime.now().minusHours(1L),
                LocalDateTime.now().plusHours(1L));
        authenticationRequest.withCallbackData(ImmutableMap.copyOf(callbackData));
        // when
        objectUnderTest.processThirdPartyCallback(new HashMap<>());
        // then
        Mockito.verify(authenticator).handleManualAuthenticationFailure();
    }

    @Test
    public void shouldWaitIfConsentNotFinal() {
        // given
        given(consentManager.getStatus()).willReturn(ConsentStatus.RCVD);

        // when
        given(consentManager.getStatus())
                .willReturn(ConsentStatus.RCVD)
                .willReturn(ConsentStatus.ACTC);
        mockTime();
        objectUnderTest.processThirdPartyCallback(new HashMap<>());

        // then
        BDDMockito.then(authenticator).should().handleManualAuthenticationSuccess();
    }

    @Test(expected = AuthorizationException.class)
    public void shouldFailIfStatusCanceled() {
        // given
        given(consentManager.getStatus()).willReturn(ConsentStatus.CANC);

        // when
        mockTime(
                LocalDateTime.now().minusHours(1L),
                LocalDateTime.now().minusHours(1L),
                LocalDateTime.now().plusHours(1L));
        objectUnderTest.processThirdPartyCallback(new HashMap<>());
        // then
        BDDMockito.then(consentManager).should(Mockito.times(2)).getStatus();
        BDDMockito.then(authenticator).should().handleManualAuthenticationFailure();
    }

    @Test
    public void shouldThrowBankSideErrorIfStatusNotFinal() {

        // given
        given(consentManager.getStatus()).willReturn(ConsentStatus.RCVD);

        // and
        mockTime(LocalDateTime.now().plusHours(1L));

        // expect
        assertThatThrownBy(() -> objectUnderTest.processThirdPartyCallback(new HashMap<>()))
                .isExactlyInstanceOf(BankServiceException.class)
                .hasMessage("Cause: BankServiceError.BANK_SIDE_FAILURE");
    }

    private void mockTime(LocalDateTime... timeValues) {
        given(localDateTimeSource.now()).willReturn(LocalDateTime.now(), timeValues);
    }
}
