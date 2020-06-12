package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.BecAuthenticationException;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BecResponseStatusHandlerTest {

    private BecResponseStatusHandler handler;

    private DefaultResponseStatusHandler defaultHandler;

    @Before
    public void setUp() {
        defaultHandler = mock(DefaultResponseStatusHandler.class);
        handler = new BecResponseStatusHandler(defaultHandler);
    }

    @Test
    public void handleNot400ResponseShouldDelegateCallToDefaultHandler() {
        // given
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        // and
        given(httpResponse.getStatus()).willReturn(200);

        // when
        handler.handleResponse(httpRequest, httpResponse);

        // then
        verify(defaultHandler).handleResponse(httpRequest, httpResponse);
    }

    @Test
    public void handle400ResponseAndScaRequestShouldThrowExceptionWithMessageTakenFromBody() {
        // given
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        // and
        given(httpRequest.getUrl()).willReturn(new URL("/samplepath/logon/SCA"));
        // and
        given(httpResponse.getStatus()).willReturn(400);
        given(httpResponse.getBody(String.class))
                .willReturn(
                        "{\"action\":"
                                + "\"R\","
                                + "\"message\":"
                                + "\"CPR no./user no. or PIN code is incorrect. "
                                + "Check in your Netbank that you are registered.\"}");

        // when
        Throwable t = catchThrowable(() -> handler.handleResponse(httpRequest, httpResponse));

        // then
        assertThat(t)
                .isInstanceOf(BecAuthenticationException.class)
                .hasMessage(
                        "CPR no./user no. or PIN code is incorrect. Check in your Netbank that you are registered.");
    }

    @Test
    public void
            handle400ResponseAndScaPrepareRequestShouldThrowExceptionWithMessageTakenFromBody() {
        // given
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        // and
        given(httpRequest.getUrl()).willReturn(new URL("/samplepath/logon/SCAprepare"));
        // and
        given(httpResponse.getStatus()).willReturn(400);
        given(httpResponse.getBody(String.class))
                .willReturn(
                        "{\"action\":"
                                + "\"R\","
                                + "\"message\":"
                                + "\"CPR no./user no. or PIN code is incorrect. "
                                + "Check in your Netbank that you are registered.\"}");

        // when
        Throwable t = catchThrowable(() -> handler.handleResponse(httpRequest, httpResponse));

        // then
        assertThat(t)
                .isInstanceOf(BecAuthenticationException.class)
                .hasMessage(
                        "CPR no./user no. or PIN code is incorrect. Check in your Netbank that you are registered.");
    }

    @Test
    public void handle400ResponseAndNonSca() {
        // given
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        // and
        given(httpRequest.getUrl()).willReturn(new URL("/samplepath/non_sca"));
        // and
        given(httpResponse.getStatus()).willReturn(400);
        given(httpResponse.getBody(String.class))
                .willReturn(
                        "{\"action\":"
                                + "\"R\","
                                + "\"message\":"
                                + "\"CPR no./user no. or PIN code is incorrect. "
                                + "Check in your Netbank that you are registered.\"}");

        // when
        handler.handleResponse(httpRequest, httpResponse);

        // then
        verify(defaultHandler).handleResponse(httpRequest, httpResponse);
    }

    @Test
    public void
            handle400ResponseFromScaEndpointShouldThrowExceptionWithUnknownErrorWhenCannotParseResponseBody() {
        // given
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        // and
        given(httpRequest.getUrl()).willReturn(new URL("/samplepath/logon/SCA"));
        // and
        given(httpResponse.getStatus()).willReturn(400);
        given(httpResponse.getBody(String.class)).willReturn(null);

        // when
        Throwable t = catchThrowable(() -> handler.handleResponse(httpRequest, httpResponse));

        // then
        assertThat(t)
                .isInstanceOf(BecAuthenticationException.class)
                .hasMessage("Unknown error occurred.");
    }

    @Test
    public void handleResponseWithoutExpectedReturnBodyShouldDelegateCallToDefaultHandler() {
        // given
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpResponse httpResponse = mock(HttpResponse.class);

        // when
        handler.handleResponseWithoutExpectedReturnBody(httpRequest, httpResponse);

        // then
        verify(defaultHandler).handleResponseWithoutExpectedReturnBody(httpRequest, httpResponse);
    }
}
