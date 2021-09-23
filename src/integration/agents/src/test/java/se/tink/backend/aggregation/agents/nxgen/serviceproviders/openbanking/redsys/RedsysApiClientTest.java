package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.when;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.AspspConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.enums.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RunWith(MockitoJUnitRunner.class)
public class RedsysApiClientTest {

    @Mock private SessionStorage sessionStorage;

    @Mock private PersistentStorage persistentStorage;

    @Mock private AspspConfiguration aspspConfiguration;

    @Mock private RedsysSignedRequestFactory signedRequestFactory;

    @Mock private AgentComponentProvider componentProvider;

    @Mock private TinkHttpClient httpClient;

    @Mock RequestBuilder requestBuilder;

    private RedsysApiClient objectUnderTest;

    @Before
    public void init() {
        when(componentProvider.getTinkHttpClient()).thenReturn(httpClient);
        when(aspspConfiguration.getAspspCode()).thenReturn("aspspFakeTestCode");
        objectUnderTest =
                new RedsysApiClient(
                        sessionStorage,
                        persistentStorage,
                        aspspConfiguration,
                        componentProvider,
                        signedRequestFactory);
    }

    @Test
    public void shouldReturnValidConsentResponse() {
        // given
        final String consentId = "12345678";
        String expectedUrl = buildConsentUrl(consentId);
        ConsentResponse consentResponse = mock(ConsentResponse.class);
        given(consentResponse.getConsentStatus()).willReturn(ConsentStatus.VALID);
        given(signedRequestFactory.createSignedRequest(expectedUrl)).willReturn(requestBuilder);
        given(requestBuilder.get(ConsentResponse.class)).willReturn(consentResponse);

        // when
        ConsentResponse response = objectUnderTest.fetchConsent(consentId);

        // then
        Assertions.assertThat(response.getConsentStatus()).isEqualTo(ConsentStatus.VALID);
    }

    @Test
    public void shouldReturnUnknownConsentResponse() {
        // given
        final String consentId = "12345678";
        String expectedUrl = buildConsentUrl(consentId);
        HttpResponseException httpResponseException = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        given(httpResponse.getStatus()).willReturn(403);
        given(httpResponse.getBody(String.class))
                .willReturn(
                        " {\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"CONSENT_UNKNOWN\",\"path\":\"/v1/consents/ca616e1e-f9e2-11eb-b6b4-1944def39949\",\"text\":\"El Consent-ID no coincide para el TPP y ASPSP que se solicit?.\"}]}\n");
        given(httpResponseException.getResponse()).willReturn(httpResponse);
        given(signedRequestFactory.createSignedRequest(expectedUrl)).willReturn(requestBuilder);
        given(requestBuilder.get(ConsentResponse.class)).willThrow(httpResponseException);

        // when
        ConsentResponse response = objectUnderTest.fetchConsent(consentId);

        // then
        Assertions.assertThat(response.getConsentStatus()).isEqualTo(ConsentStatus.UNKNOWN);
    }

    @Test
    public void shouldRethrowHttpResponseException() {
        // given
        final String consentId = "12345678";
        String expectedUrl = buildConsentUrl(consentId);
        HttpResponseException httpResponseException = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        given(httpResponse.getStatus()).willReturn(403);
        given(httpResponse.getBody(String.class))
                .willReturn("{\"errorMessage\": \"fake test message\"}");
        given(httpResponseException.getResponse()).willReturn(httpResponse);
        given(signedRequestFactory.createSignedRequest(expectedUrl)).willReturn(requestBuilder);
        given(requestBuilder.get(ConsentResponse.class)).willThrow(httpResponseException);

        // when
        Throwable throwable =
                Assertions.catchThrowable(() -> objectUnderTest.fetchConsent(consentId));

        // then
        Assertions.assertThat(throwable).isInstanceOf(HttpResponseException.class);
        Assertions.assertThat(((HttpResponseException) throwable).getResponse().getStatus())
                .isEqualTo(403);
    }

    private String buildConsentUrl(String consentId) {
        return String.format(
                "%s/%s%s",
                RedsysConstants.Urls.BASE_API_URL,
                aspspConfiguration.getAspspCode(),
                String.format(RedsysConstants.Urls.CONSENT, consentId));
    }
}
