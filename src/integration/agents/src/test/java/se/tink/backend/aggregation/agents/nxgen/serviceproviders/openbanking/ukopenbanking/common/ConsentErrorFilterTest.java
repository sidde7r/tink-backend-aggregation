package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.UkObErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(MockitoJUnitRunner.class)
public class ConsentErrorFilterTest {

    @Mock private HttpRequest httpRequest;
    @Mock private Filter filter;
    private ConsentErrorFilter consentErrorFilter;

    @Before
    public void setUp() throws Exception {
        this.consentErrorFilter = new ConsentErrorFilter(mock(PersistentStorage.class));
        consentErrorFilter.setNext(filter);
        given(httpRequest.getUrl()).willReturn(new URL("DUMMY_PATH"));
    }

    @Test
    public void shouldReturnAuthorisedConsent() throws Exception {
        // given
        ConsentResponse consentResponse =
                objectFromString(
                        "{\"Data\":{\"ConsentId\":\"DUMMY_CONSENT_ID\",\"Permissions\":[\"ReadAccountsDetail\",\"ReadTransactionsCredits\",\"ReadDirectDebits\",\"ReadTransactionsDebits\",\"ReadBeneficiariesDetail\",\"ReadBalances\",\"ReadStandingOrdersDetail\",\"ReadTransactionsDetail\"],\"CreationDateTime\":\"2021-02-18T14:40:42.485Z\",\"StatusUpdateDateTime\": \"2021-02-18T14:40:42.485Z\",\"Status\":\"Authorised\"},\"Risk\":{},\"Links\":{\"Self\":\"https://api.ulsterbank.co.uk/open-banking/v3.1/aisp/account-access-consents/DUMMY_CONSENT_ID\"},\"Meta\":{}}",
                        ConsentResponse.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        given(httpResponse.getStatus()).willReturn(200);
        given(httpResponse.getBody(ConsentResponse.class)).willReturn(consentResponse);
        given(filter.handle(any())).willReturn(httpResponse);

        // when
        HttpResponse response = consentErrorFilter.handle(httpRequest);

        // then
        assertThat(response.getBody(ConsentResponse.class).getConsentId())
                .isEqualTo("DUMMY_CONSENT_ID");
    }

    @Test
    public void shouldThrowSessionExceptionForNotFoundConsent() throws Exception {
        // given
        String responseMessage =
                "{\"Code\":\"400 BadRequest\",\"Message\":\"Invalid request parameters.\",\"Errors\":[{\"ErrorCode\":\"UK.OBIE.Resource.NotFound\",\"Message\":\"An invalid consent identifier was supplied.\"}]}";
        mockResponse(400, responseMessage);

        // when
        Throwable thrown = catchThrowable(() -> consentErrorFilter.handle(httpRequest));

        // then
        assertThat(thrown)
                .isInstanceOf(SessionError.CONSENT_INVALID.exception().getClass())
                .hasMessage(
                        "[ConsentErrorFilter] The consent error occurred for path: `DUMMY_PATH`, with HTTP status: `400` and ErrorCodes:`[UK.OBIE.Resource.NotFound]`");
    }

    @Test
    public void shouldThrowSessionExceptionForReauthenticateConsent() throws Exception {
        // given
        String responseMessage =
                "{\"Code\":\"403 Forbidden\",\"Message\":\"Customer needs to Reauthenticate.\",\"Errors\":[{\"ErrorCode\":\"UK.OBIE.Reauthenticate\",\"Message\":\"Customer needs to Reauthenticate.\"}]}";
        mockResponse(403, responseMessage);

        // when
        Throwable thrown = catchThrowable(() -> consentErrorFilter.handle(httpRequest));

        // then
        assertThat(thrown)
                .isInstanceOf(SessionError.CONSENT_INVALID.exception().getClass())
                .hasMessage(
                        "[ConsentErrorFilter] The consent error occurred for path: `DUMMY_PATH`, with HTTP status: `403` and ErrorCodes:`[UK.OBIE.Reauthenticate]`");
    }

    @Test
    public void shouldThrowSessionExceptionForInvalidConsent() throws Exception {
        // given
        String responseMessage =
                "{\"Code\":\"400 Bad Request\",\"Id\":\"2c7cb790-a388-43c3-9062-4a24fd478ef8\",\"Message\":\"Consent validation failed. \",\"Errors\":[{\"ErrorCode\":\"UK.OBIE.Resource.InvalidConsentStatus\",\"Message\":\"The requested Consent ID doesn't exist or do not have valid status. \"}]}";
        mockResponse(400, responseMessage);

        // when
        Throwable thrown = catchThrowable(() -> consentErrorFilter.handle(httpRequest));

        // then
        assertThat(thrown)
                .isInstanceOf(SessionError.CONSENT_INVALID.exception().getClass())
                .hasMessage(
                        "[ConsentErrorFilter] The consent error occurred for path: `DUMMY_PATH`, with HTTP status: `400` and ErrorCodes:`[UK.OBIE.Resource.InvalidConsentStatus]`");
    }

    private void mockResponse(int httpStatus, String messageResponse) throws Exception {
        UkObErrorResponse responseBody = objectFromString(messageResponse, UkObErrorResponse.class);
        HttpResponse response = mock(HttpResponse.class);
        given(response.getStatus()).willReturn(httpStatus);
        given(response.hasBody()).willReturn(true);
        given(response.getBody(UkObErrorResponse.class)).willReturn(responseBody);
        given(filter.handle(any())).willReturn(response);
    }

    private <T> T objectFromString(String jsonString, Class<T> tClass) throws Exception {
        return new ObjectMapper().readValue(jsonString, tClass);
    }
}
