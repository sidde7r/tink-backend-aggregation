package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(MockitoJUnitRunner.class)
public class DanskeBankDKApiClientTest {

    private static final String SECURITY_SYSTEM = "-- security system --";
    private static final String BRAND = "-- brand --";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TinkHttpClient client;

    @Mock private DanskeBankDKConfiguration configuration;

    @Mock private Credentials credentials;

    @InjectMocks private DanskeBankDKApiClient apiClient;

    @Test
    public void collectDynamicLogonJavascriptShouldReturnResultFromBaseClass() {
        // given
        HttpResponse response = mock(HttpResponse.class);
        // and
        given(client.request(anyString()).header(anyString(), any()).get(HttpResponse.class))
                .willReturn(response);

        // when
        HttpResponse result = apiClient.collectDynamicLogonJavascript(SECURITY_SYSTEM, BRAND);

        // then
        assertThat(result).isEqualTo(response);
    }

    @Test
    public void
            collectDynamicLogonJavascriptShouldThrowISEWhenBankRespondWith412StatusAndUnauthorizedClientMessage() {
        // given
        HttpResponse response =
                response412(
                        "{\"httpCode\":412,\"httpMessage\":\"Unauthorized\",\"moreInformation\":\"Unauthorized client\"}");
        // and
        given(client.request(anyString())).willThrow(new HttpResponseException(null, response));

        // when
        Throwable t =
                catchThrowable(
                        () -> apiClient.collectDynamicLogonJavascript(SECURITY_SYSTEM, BRAND));

        // then
        assertThat(t)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Check if DanskebankDK has rotated their clientSecret");
    }

    @Test
    public void
            collectDynamicLogonJavascriptShouldIgnoreCaseWhenBankRespondsWith412AndUnauthorizedClientMessage() {
        // given
        HttpResponse response = response412("UnAUthoRIzED CLIEnt");
        // and
        given(client.request(anyString())).willThrow(new HttpResponseException(null, response));

        // when
        Throwable t =
                catchThrowable(
                        () -> apiClient.collectDynamicLogonJavascript(SECURITY_SYSTEM, BRAND));

        // then
        assertThat(t)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Check if DanskebankDK has rotated their clientSecret");
    }

    @Test
    public void
            collectDynamicLogonJavascriptShouldRethrowHttpResponseExceptionWhenStatusIsNot412() {
        // given
        HttpResponse response = responseUnauthorizedClient(200);

        // and
        given(client.request(anyString())).willThrow(new HttpResponseException(null, response));

        // when
        Throwable t =
                catchThrowable(
                        () -> apiClient.collectDynamicLogonJavascript(SECURITY_SYSTEM, BRAND));

        // then
        assertThat(t).isInstanceOf(HttpResponseException.class);
    }

    @Test
    public void
            collectDynamicLogonJavascriptShouldRethrowHttpResponseExceptionWhenMessageNotContainUnauthorizedClientMsg() {
        // given
        HttpResponse response = response412("sample message");

        // and
        given(client.request(anyString())).willThrow(new HttpResponseException(null, response));

        // when
        Throwable t =
                catchThrowable(
                        () -> apiClient.collectDynamicLogonJavascript(SECURITY_SYSTEM, BRAND));

        // then
        assertThat(t).isInstanceOf(HttpResponseException.class);
    }

    private HttpResponse response412(final String message) {
        return clientResponse(message, 412);
    }

    private HttpResponse responseUnauthorizedClient(final int status) {
        return clientResponse("Unauthorized client", status);
    }

    private HttpResponse clientResponse(final String message, final int status) {
        HttpResponse response = mock(HttpResponse.class);
        given(response.getStatus()).willReturn(status);
        given(response.getBody(String.class)).willReturn(message);
        return response;
    }
}
