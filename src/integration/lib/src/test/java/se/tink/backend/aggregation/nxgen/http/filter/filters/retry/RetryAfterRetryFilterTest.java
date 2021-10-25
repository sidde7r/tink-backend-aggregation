package se.tink.backend.aggregation.nxgen.http.filter.filters.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.jersey.core.header.InBoundHeaders;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(JUnitParamsRunner.class)
public class RetryAfterRetryFilterTest {

    private RetryAfterRetryFilter retryFilter;

    @Test
    public void should_return_correct_retry_sleep_milliseconds() {
        // given
        retryFilter = new RetryAfterRetryFilter(3);
        List<Pair<HttpResponse, Long>> responsesWithRetrySleepMillis = getRetrySleepMillisParams();

        // when
        int retriesCounter = 0;

        for (Pair<HttpResponse, Long> responseWithRetrySleepMillis :
                responsesWithRetrySleepMillis) {
            HttpResponse response = responseWithRetrySleepMillis.getLeft();
            Long expectedRetrySleepMillis = responseWithRetrySleepMillis.getRight();

            boolean shouldRetry = retryFilter.shouldRetry(response);
            long retrySleepMillis = retryFilter.getRetrySleepMilliseconds(retriesCounter++);

            // then
            assertThat(shouldRetry).isTrue();
            assertThat(retrySleepMillis).isEqualTo(expectedRetrySleepMillis);
        }
    }

    private static List<Pair<HttpResponse, Long>> getRetrySleepMillisParams() {
        return Stream.of(
                        Pair.of(
                                HttpResponseConfig.builder()
                                        .status(429)
                                        .addHeader("Retry-After", "100")
                                        .build()
                                        .createMockHttpResponse(),
                                100 * 1000L),
                        Pair.of(
                                HttpResponseConfig.builder()
                                        .status(429)
                                        .addHeader("Retry-After", "50")
                                        .build()
                                        .createMockHttpResponse(),
                                50 * 1000L),
                        Pair.of(
                                HttpResponseConfig.builder()
                                        .status(429)
                                        .addHeader("Retry-After", "100")
                                        .build()
                                        .createMockHttpResponse(),
                                100 * 1000L))
                .collect(Collectors.toList());
    }

    @Test
    @Parameters(method = "exampleHttpExceptions")
    public void should_not_filter_responses_with_exceptions(HttpClientException exception) {
        // given
        retryFilter = new RetryAfterRetryFilter(3);

        // when
        boolean shouldRetry = retryFilter.shouldRetry(exception);

        // then
        assertThat(shouldRetry).isFalse();
    }

    @SuppressWarnings("unused")
    private static Object[] exampleHttpExceptions() {
        HttpRequest request = mock(HttpRequest.class);
        return new Object[] {
            new Object[] {new HttpClientException(new SSLException("reason 1"), request)},
            new Object[] {new HttpClientException(new SSLHandshakeException("reason 2"), request)},
            new Object[] {new HttpClientException("connection reset", mock(HttpRequest.class))}
        };
    }

    @Test
    @Parameters(method = "notMatchingResponses")
    public void should_not_filter_not_matching_responses(HttpResponse notMatchingResponse) {
        // given
        retryFilter = new RetryAfterRetryFilter(3);

        // when
        boolean shouldRetry = retryFilter.shouldRetry(notMatchingResponse);

        // then
        assertThat(shouldRetry).isFalse();
    }

    @SuppressWarnings("unused")
    private static Object[] notMatchingResponses() {
        HttpResponseConfig matchingResponseConfig =
                HttpResponseConfig.builder().status(429).addHeader("Retry-After", "100").build();

        List<HttpResponseConfig> configsWithNotMatchingStatus =
                Stream.of(
                                matchingResponseConfig.toBuilder().status(200).build(),
                                matchingResponseConfig.toBuilder().status(400).build(),
                                matchingResponseConfig.toBuilder().status(401).build(),
                                matchingResponseConfig.toBuilder().status(500).build())
                        .collect(Collectors.toList());

        List<HttpResponseConfig> configsWithMissingHeader =
                Stream.of(
                                HttpResponseConfig.builder().status(429).build(),
                                HttpResponseConfig.builder()
                                        .status(429)
                                        .addHeader("RetryAfter", "100")
                                        .build())
                        .collect(Collectors.toList());

        List<HttpResponseConfig> configsWithInvalidHeader =
                Stream.of(
                                // not a number
                                HttpResponseConfig.builder()
                                        .status(429)
                                        .addHeader("Retry-After", "abc")
                                        .build(),
                                // negative value
                                HttpResponseConfig.builder()
                                        .status(429)
                                        .addHeader("Retry-After", "-1")
                                        .build())
                        .collect(Collectors.toList());

        return Stream.of(
                        configsWithNotMatchingStatus,
                        configsWithMissingHeader,
                        configsWithInvalidHeader)
                .flatMap(List::stream)
                .map(HttpResponseConfig::createMockHttpResponse)
                .map(response -> new Object[] {response})
                .toArray();
    }

    @Builder(builderClassName = "HttpResponseConfigBuilder", toBuilder = true)
    private static class HttpResponseConfig {

        private final int status;
        private final InBoundHeaders headers;

        private HttpResponse createMockHttpResponse() {
            HttpResponse httpResponse = mock(HttpResponse.class);
            when(httpResponse.getStatus()).thenReturn(status);
            when(httpResponse.getHeaders()).thenReturn(headers);
            return httpResponse;
        }

        // Override default lombok builder
        @SuppressWarnings("unused")
        private static class HttpResponseConfigBuilder {

            private HttpResponseConfigBuilder() {
                this.headers = new InBoundHeaders();
                this.status = 200;
            }

            private HttpResponseConfigBuilder addHeader(String key, String value) {
                headers.putSingleObject(key, value);
                return this;
            }

            private HttpResponseConfigBuilder removeHeader(String key) {
                headers.remove(key);
                return this;
            }

            private HttpResponseConfig build() {
                return new HttpResponseConfig(status, headers);
            }
        }
    }
}
