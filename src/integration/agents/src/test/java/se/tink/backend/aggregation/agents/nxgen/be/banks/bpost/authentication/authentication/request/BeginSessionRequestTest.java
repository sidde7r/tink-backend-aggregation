package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.WireMockIntegrationTest;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class BeginSessionRequestTest extends WireMockIntegrationTest {

    private BeginSessionRequest objectUnderTest;
    private RequestBuilder requestBuilder;

    @Before
    public void init() {
        requestBuilder = httpClient.request(getOrigin() + BeginSessionRequest.URL_PATH);
        objectUnderTest = new BeginSessionRequest();
    }

    @Test
    public void shouldReturnCSRFTokenFromResponseHeader() throws RequestException {
        // given
        final String csrfToken = "44807660-4851-4ada-b59c-4feaa06b425d";
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/bpb/services/rest/v2/session"))
                        .willReturn(WireMock.aResponse().withHeader("X-BBXSRF", csrfToken)));
        // when
        String result = objectUnderTest.execute(requestBuilder);
        // then
        Assert.assertEquals(csrfToken, result);
    }

    @Test
    public void shouldThrowAuthenticationExceptionWhenThereIsNotCSRFTokenInResponse()
            throws RequestException {
        // given
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/bpb/services/rest/v2/session"))
                        .willReturn(WireMock.aResponse()));
        // when
        Throwable thrown = Assertions.catchThrowable(() -> objectUnderTest.execute(requestBuilder));
        // then
        Assertions.assertThat(thrown).isInstanceOf(RequestException.class);
    }
}
