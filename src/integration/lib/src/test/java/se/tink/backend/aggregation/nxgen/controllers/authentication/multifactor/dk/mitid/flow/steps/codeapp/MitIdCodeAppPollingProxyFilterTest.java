package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.codeapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import lombok.ToString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.integration.webdriver.service.proxy.ProxyResponse;

@RunWith(JUnitParamsRunner.class)
public class MitIdCodeAppPollingProxyFilterTest {

    private MitIdCodeAppPollingProxyFilter listener;

    @Before
    public void setup() {
        listener = new MitIdCodeAppPollingProxyFilter();
    }

    @Test
    @Parameters(method = "paramsForShouldRecognizeCorrectPollingResult")
    public void should_recognize_correct_polling_result(
            ProxyResponseMock proxyResponseMock,
            @Nullable MitIdCodeAppPollingResult expectedResult) {
        // given
        ProxyResponse proxyResponse = proxyResponseMock.getMockResponse();

        // when
        listener.handleResponse(proxyResponse);
        Optional<MitIdCodeAppPollingResult> pollingResult = listener.waitForResult(0);

        // then
        if (expectedResult != null) {
            assertThat(pollingResult).hasValue(expectedResult);
        } else {
            assertThat(pollingResult).isEmpty();
        }
    }

    @SuppressWarnings("unused")
    private static Object[] paramsForShouldRecognizeCorrectPollingResult() {
        return new Object[] {
            asArray(
                    ProxyResponseMock.builder()
                            .url("example.com/poll")
                            .httpMethod("POST")
                            .status(200)
                            .body("{\"status\": \"ok\", \"confirmation\": true}")
                            .build(),
                    MitIdCodeAppPollingResult.OK),
            asArray(
                    ProxyResponseMock.builder()
                            .url("example.com/prefix/poll/suffix1/suffix2")
                            .httpMethod("POST")
                            .status(200)
                            .body("{\"status\": \"ok\", \"confirmation\": true}")
                            .build(),
                    MitIdCodeAppPollingResult.OK),
            asArray(
                    ProxyResponseMock.builder()
                            .url("example.com/prefix/poll/suffix1/suffix2")
                            .httpMethod("GET")
                            .status(200)
                            .body("{\"status\": \"ok\", \"confirmation\": true}")
                            .build(),
                    null),
            asArray(
                    ProxyResponseMock.builder()
                            .url("some.example.com/prefix/poll/suffix")
                            .httpMethod("POST")
                            .status(200)
                            .body("{\"status\": \"ok\", \"confirmation\": false}")
                            .build(),
                    MitIdCodeAppPollingResult.REJECTED),
            asArray(
                    ProxyResponseMock.builder()
                            .url("example.com/poll")
                            .httpMethod("POST")
                            .status(200)
                            .body("{\"status\": \"expired\"}")
                            .build(),
                    MitIdCodeAppPollingResult.EXPIRED),
            asArray(
                    ProxyResponseMock.builder()
                            .url("example.com/poll")
                            .httpMethod("POST")
                            .status(200)
                            .body("{\"status\": \"timeout\"}")
                            .build(),
                    null),
            asArray(
                    ProxyResponseMock.builder()
                            .url("example.com/poll")
                            .httpMethod("POST")
                            .status(200)
                            .body("{\"status\": \"someUnknownStatus\"}")
                            .build(),
                    MitIdCodeAppPollingResult.UNKNOWN),
            asArray(
                    ProxyResponseMock.builder()
                            .url("example.com/poll")
                            .httpMethod("POST")
                            .status(404)
                            .body("{}")
                            .build(),
                    MitIdCodeAppPollingResult.TECHNICAL_ERROR)
        };
    }

    @Test
    public void should_allow_waiting_for_polling_result() {
        // given
        ProxyResponse proxyResponse =
                ProxyResponseMock.builder()
                        .url("example.com/poll")
                        .httpMethod("POST")
                        .status(200)
                        .body("{\"status\": \"ok\", \"confirmation\": true}")
                        .build()
                        .getMockResponse();

        // when
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(() -> listener.handleResponse(proxyResponse), 100, TimeUnit.MILLISECONDS);

        Optional<MitIdCodeAppPollingResult> pollingResult = listener.waitForResult(1);

        // then
        assertThat(pollingResult).hasValue(MitIdCodeAppPollingResult.OK);
    }

    @ToString
    @Builder
    private static class ProxyResponseMock {
        private final String url;
        private final String httpMethod;
        private final int status;
        private final String body;

        private ProxyResponse getMockResponse() {
            ProxyResponse response = Mockito.mock(ProxyResponse.class, Mockito.RETURNS_DEEP_STUBS);
            when(response.getMessageInfo().getUrl()).thenReturn(url);
            when(response.getMessageInfo().getOriginalRequest().method().name())
                    .thenReturn(httpMethod);
            when(response.getResponse().status().code()).thenReturn(status);
            when(response.getContents().getTextContents()).thenReturn(body);
            return response;
        }
    }

    private static Object[] asArray(Object... values) {
        return values;
    }
}
