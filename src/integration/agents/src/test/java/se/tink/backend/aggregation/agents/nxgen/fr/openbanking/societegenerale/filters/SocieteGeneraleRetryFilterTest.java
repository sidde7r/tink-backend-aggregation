package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.filters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.RetryFilter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SocieteGeneraleRetryFilterTest {
    private SocieteGeneraleRetryFilter objectUnderTest;

    @Before
    public void setUp() {
        objectUnderTest =
                new SocieteGeneraleRetryFilter(
                        RetryFilter.NUM_TIMEOUT_RETRIES, RetryFilter.RETRY_SLEEP_MILLISECONDS);
    }

    @Test
    public void shouldReturnTrueForServiceUnavailableStatus() {
        // given
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(503);
        // when
        boolean shouldRetry = objectUnderTest.shouldRetry(httpResponse);
        // then
        Assertions.assertThat(shouldRetry).isTrue();
    }

    @Test
    public void shouldReturnTrueForGatewayTimeout() {
        // given
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(504);
        when(httpResponse.getBody(ErrorResponse.class))
                .thenReturn(prepareErrorResponseWithGatewayTimeout());
        // when
        boolean shouldRetry = objectUnderTest.shouldRetry(httpResponse);
        // then
        Assertions.assertThat(shouldRetry).isTrue();
    }

    private ErrorResponse prepareErrorResponseWithGatewayTimeout() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"timestamp\": \"2021-10-06T10:11:47.052+00:00\",\n"
                        + "  \"path\": \"/sg/prod/pri/v1.4.2.17/psd2/xs2a/end-user-identity\",\n"
                        + "  \"status\": 504,\n"
                        + "  \"error\": \"Gateway Timeout\",\n"
                        + "  \"message\": \"Response took longer than timeout: PT10S\",\n"
                        + "  \"requestId\": \"120123ab-123\",\n"
                        + "  \"traceId\": \"a1a123a12a123a12\"\n"
                        + "}",
                ErrorResponse.class);
    }
}
