package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.BecAuthenticationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.filter.ScaAuthenticationErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RunWith(JUnitParamsRunner.class)
public class ScaAuthenticationErrorFilterTest {

    private ScaAuthenticationErrorFilter scaAuthenticationErrorFilter;

    private final HttpRequest httpRequest = Mockito.mock(HttpRequest.class);
    private HttpResponse httpResponse;

    @Before
    public void setUp() {
        Filter mockFilter = mock(Filter.class);
        scaAuthenticationErrorFilter = new ScaAuthenticationErrorFilter();
        scaAuthenticationErrorFilter.setNext(mockFilter);
        httpResponse = Mockito.mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(400);
        when(httpResponse.hasBody()).thenReturn(true);
        when(mockFilter.handle(httpRequest)).thenReturn(httpResponse);
    }

    @Test
    public void handle200ResponseCorrectly() {
        // given
        given(httpResponse.getStatus()).willReturn(200);

        // when
        HttpResponse resultResponse = scaAuthenticationErrorFilter.handle(httpRequest);

        // then
        assertThat(resultResponse).isEqualTo(httpResponse);
    }

    @Test
    public void handle400ResponseAndIncorrectCredentialsMessage() {
        // given
        given(httpRequest.getUrl()).willReturn(new URL("/samplepath/logon/SCA"));
        given(httpResponse.getStatus()).willReturn(400);
        given(httpResponse.getBody(String.class))
                .willReturn(
                        "{\"action\":"
                                + "\"R\","
                                + "\"message\":"
                                + "\"CPR no./user no. or PIN code is incorrect. "
                                + "Check in your Netbank that you are registered.\"}");

        // when
        Throwable t = catchThrowable(() -> scaAuthenticationErrorFilter.handle(httpRequest));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessageContaining("LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void handle400ResponseAndFunctionNotAvailableMessage() {
        // given
        given(httpRequest.getUrl()).willReturn(new URL("/samplepath/logon/SCAprepare"));
        given(httpResponse.getStatus()).willReturn(400);
        given(httpResponse.getBody(String.class))
                .willReturn(
                        "{\"action\":"
                                + "\"R\","
                                + "\"message\":"
                                + "\"The required function is not currently available. Try again later.\"}");

        // when
        Throwable t = catchThrowable(() -> scaAuthenticationErrorFilter.handle(httpRequest));

        // then
        assertThat(t)
                .isInstanceOf(BankServiceException.class)
                .hasMessageContaining(
                        "The required function is not currently available. Try again later.");
    }

    @Test
    public void handle400ResponseAndResetTokenMessage() {
        // given
        given(httpRequest.getUrl()).willReturn(new URL("/samplepath/logon/SCA"));
        given(httpResponse.getStatus()).willReturn(400);
        given(httpResponse.getBody(String.class))
                .willReturn(
                        "{\"action\":"
                                + "\"R\","
                                + "\"message\":"
                                + "\"error auth response: Reset token\"}");

        // when
        Throwable t = catchThrowable(() -> scaAuthenticationErrorFilter.handle(httpRequest));

        // then
        assertThat(t)
                .isInstanceOf(SessionException.class)
                .hasMessageContaining("SessionError.CONSENT_REVOKED");
    }

    @Test
    public void
            handle400ResponseFromScaEndpointShouldThrowExceptionWithUnknownErrorWhenCannotParseResponseBody() {
        // given
        given(httpRequest.getUrl()).willReturn(new URL("/samplepath/logon/SCA"));
        given(httpResponse.getStatus()).willReturn(400);
        given(httpResponse.getBody(String.class)).willReturn(null);

        // when
        Throwable t = catchThrowable(() -> scaAuthenticationErrorFilter.handle(httpRequest));

        // then
        assertThat(t)
                .isInstanceOf(BecAuthenticationException.class)
                .hasMessage("Unknown error occurred.");
    }
}
