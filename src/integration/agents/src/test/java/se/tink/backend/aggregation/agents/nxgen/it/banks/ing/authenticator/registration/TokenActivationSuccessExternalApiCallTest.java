package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestAsserts.assertHttpRequestsEquals;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.givenActivationId;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.givenBaseUrl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.TokenActivationSuccessExternalApiCall.Arg;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.TokenActivationSuccessExternalApiCall.Result;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;

public class TokenActivationSuccessExternalApiCallTest {

    private TinkHttpClient httpClient = mock(TinkHttpClient.class);
    private ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);
    private TokenActivationSuccessExternalApiCall sut =
            new TokenActivationSuccessExternalApiCall(httpClient, configurationProvider);

    @Before
    public void setupMock() {
        Mockito.reset(httpClient);
        Mockito.reset(configurationProvider);
    }

    @Test
    public void prepareRequestShouldReturnProperHttpRequestWhenProperArgPassed() {
        // given
        Arg arg = Arg.builder().activationId(givenActivationId()).build();
        when(configurationProvider.getBaseUrl()).thenReturn(givenBaseUrl());

        // when
        HttpRequest httpRequest = sut.prepareRequest(arg);

        // then
        assertHttpRequestsEquals(httpRequest, givenHttpRequest());
    }

    @Test
    public void parseResponseShouldReturnProperResultWhenHttpResponsePassed() {
        // given
        ExternalApiCallResult<Result> givenExternalApiCallResult =
                ExternalApiCallResult.of(Result.builder().build(), 200);

        HttpResponse givenHttpResponse = mock(HttpResponse.class);
        when(givenHttpResponse.getStatus()).thenReturn(200);
        when(givenHttpResponse.getBody(String.class)).thenReturn(givenResponseBody());

        // when
        ExternalApiCallResult<Result> actualResult = sut.parseResponse(givenHttpResponse);

        // then
        assertThat(actualResult).isEqualTo(givenExternalApiCallResult);
    }

    private static HttpRequest givenHttpRequest() {
        return new HttpRequestImpl(
                HttpMethod.POST,
                new URL(givenBaseUrl() + "/MobileFlow/tokenActivationSuccess.htm"),
                String.format(
                        "succesLabel=%s&aid=%s&oid=%s&otml_context=%s",
                        "Complimenti%2C%20il%20Token%20%C3%A8%20stato%20correttamente%20attivato%20su%20questo%20dispositivo.",
                        givenActivationId(),
                        givenActivationId(),
                        "c1"));
    }

    private static String givenResponseBody() {
        return "{\"datasources\":\"<datasources><datasource key=\\\"otml_store_session\\\"><element key=\\\"aPlusRateDate\\\" val=\\\"Tassi validi ad oggi\\\" /><\\/datasource><\\/datasources>\",\"xml\":\"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\" standalone=\\\"yes\\\"?>\\n<map version=\\\"1\\\" name=\\\"dummymap\\\"><elements><element key=\\\"dummymap\\\"><entry canGoBack=\\\"false\\\"><layout><linear_container orientation=\\\"v\\\" width=\\\"fill\\\" height=\\\"wrap\\\"/><\\/layout><\\/entry><\\/element><\\/elements><\\/map>\\n\",\"params\":{\"otml_context\":\"c1\",\"otml_stack\":\"push.otml_piggybacking\"},\"target\":\"activatemobiletoken3\"}";
    }
}
