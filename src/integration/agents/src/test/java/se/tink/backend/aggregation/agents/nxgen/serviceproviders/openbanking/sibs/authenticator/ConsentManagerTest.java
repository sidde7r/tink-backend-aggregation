package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ConsentManagerTest {

    private SibsBaseApiClient apiClient;
    private SibsUserState userState;
    private StrongAuthenticationState strongAuthenticationState;
    private ConsentManager objectUnderTest;

    @Before
    public void init() {
        apiClient = Mockito.mock(SibsBaseApiClient.class);
        userState = Mockito.mock(SibsUserState.class);
        strongAuthenticationState = Mockito.mock(StrongAuthenticationState.class);
        objectUnderTest = new ConsentManager(apiClient, userState, strongAuthenticationState);
    }

    @Test(expected = SessionException.class)
    public void
            getConsentStatusShouldThrowSessionExpiredExceptionWhenExceptionOccuredDuringCheckingConsentStatus()
                    throws SessionException {
        // given
        HttpResponseException ex = Mockito.mock(HttpResponseException.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(ex.getResponse()).thenReturn(httpResponse);
        Mockito.when(httpResponse.getBody(String.class))
                .thenReturn(MessageCodes.CONSENT_EXPIRED.name());
        Mockito.when(apiClient.getConsentStatus()).thenThrow(ex);
        // when
        objectUnderTest.getStatus();
        // then
        Mockito.verify(userState).resetAuthenticationState();
    }

    @Test()
    public void getConsentStatusShouldConsentStatus() throws SessionException {
        // given
        Mockito.when(apiClient.getConsentStatus()).thenReturn(ConsentStatus.RJCT);
        // when
        ConsentStatus result = objectUnderTest.getStatus();
        // then
        Assert.assertEquals(ConsentStatus.RJCT, result);
    }

    @Test
    public void createShouldReturnRedirectURLAndStartManualAuthenticationProcess() {
        // given
        final String state = "strongState";
        final String redirectUrl = "http://127.0.0.1";
        final String consentId = "32142342315341";
        Mockito.when(strongAuthenticationState.getState()).thenReturn(state);
        ConsentResponse consentResponse = Mockito.mock(ConsentResponse.class);
        Mockito.when(consentResponse.getConsentId()).thenReturn(consentId);
        ConsentLinksEntity entity = Mockito.mock(ConsentLinksEntity.class);
        Mockito.when(entity.getRedirect()).thenReturn(redirectUrl);
        Mockito.when(consentResponse.getLinks()).thenReturn(entity);
        Mockito.when(apiClient.createConsent(state)).thenReturn(consentResponse);
        // when
        URL url = objectUnderTest.create();
        // then
        Assert.assertEquals(redirectUrl, url.getUrl().get());
        Mockito.verify(userState).startManualAuthentication(consentId);
    }

    @Test
    public void shouldThrowBankServiceExceptionForRateLimitExceededResponse() {
        // given
        HttpResponseException httpResponseException = Mockito.mock(HttpResponseException.class);
        HttpResponse rateLimitExceededResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(rateLimitExceededResponse.getStatus()).thenReturn(429);
        Mockito.when(httpResponseException.getResponse()).thenReturn(rateLimitExceededResponse);
        Mockito.when(apiClient.getConsentStatus()).thenThrow(httpResponseException);

        // when
        Throwable thrown = Assertions.catchThrowable(() -> objectUnderTest.getStatus());

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(BankServiceException.class)
                .hasMessageContaining(BankServiceError.ACCESS_EXCEEDED.name());
    }
}
