package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestAsserts.assertHttpRequestsEquals;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.givenActivationId;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.givenBaseUrl;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures.givenChallenge;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.TokenLoginActivateExternalApiCall.Arg;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.TokenLoginActivateExternalApiCall.Result;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class TokenLoginActivateExternalApiCallTest {

    private TinkHttpClient httpClient = mock(TinkHttpClient.class);
    private Clock fixedClock =
            Clock.fixed(Instant.parse("2019-12-01T10:15:30.00Z"), ZoneId.of("Europe/Warsaw"));
    private ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);
    private TokenLoginActivateExternalApiCall sut =
            new TokenLoginActivateExternalApiCall(httpClient, configurationProvider, fixedClock);

    @Before
    public void setupMock() {
        Mockito.reset(httpClient);
        Mockito.reset(configurationProvider);
    }

    @Test
    public void prepareRequestShouldReturnProperHttpRequestWhenProperArgPassed() {
        // given
        String givenContext = "c1";
        String mtActionSignDataiopPart = "111461CB25554711A731B247A1136168";
        String mtActionSignOtp = "584111";
        Arg arg =
                Arg.builder()
                        .activationId(givenActivationId())
                        .challengeId(givenChallenge())
                        .mtActionSignDataiopPart(mtActionSignDataiopPart)
                        .mtActionSignOtp(mtActionSignOtp)
                        .build();
        HttpRequest givenHttpRequest =
                givenHttpRequest(
                        fixedClock, givenContext, mtActionSignOtp, mtActionSignDataiopPart);
        when(configurationProvider.getBaseUrl()).thenReturn(givenBaseUrl());

        // when
        HttpRequest httpRequest = sut.prepareRequest(arg);

        // then
        assertHttpRequestsEquals(httpRequest, givenHttpRequest);
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

    private static HttpRequest givenHttpRequest(
            Clock clock,
            String givenContext,
            String mtActionSignOtp,
            String mtActionSignDataiopPart) {
        return new HttpRequestImpl(
                HttpMethod.POST,
                new URL(givenBaseUrl() + "/MobileFlow/tokenLoginActivate.htm"),
                String.format(
                        "oid=%s&otml_context=%s&challengeId=%s&mt_action_sign_otp=%s&mt_action_sign_datiop=%s&aid=%s",
                        givenActivationId(),
                        givenContext,
                        givenChallenge(),
                        mtActionSignOtp,
                        mtActionSignDataiopPart + "%7C" + "1" + "%7C" + clock.millis(),
                        givenActivationId()));
    }

    private static String givenResponseBody() {
        return "{\"datasources\":\"<datasources><datasource key=\\\"otml_store_session\\\"><element key=\\\"aPlusRateDate\\\" val=\\\"Tassi validi ad oggi\\\" /><\\/datasource><\\/datasources>\",\"xml\":\"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\" standalone=\\\"yes\\\"?>\\n<map version=\\\"1\\\" name=\\\"dummymap\\\"><elements><element key=\\\"dummymap\\\"><entry canGoBack=\\\"false\\\"><layout><linear_container orientation=\\\"v\\\" width=\\\"fill\\\" height=\\\"wrap\\\"/><\\/layout><\\/entry><\\/element><\\/elements><\\/map>\\n\",\"params\":{\"otml_context\":\"c1\",\"otml_stack\":\"push.otml_piggybacking\"},\"target\":\"dummymap\"}";
    }
}
