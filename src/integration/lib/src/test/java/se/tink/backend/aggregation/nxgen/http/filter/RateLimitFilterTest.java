package se.tink.backend.aggregation.nxgen.http.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.RateLimitFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.rate_limit_service.RateLimitService;

public class RateLimitFilterTest {
    private String providerName;

    @Before
    public void setUp() {
        // RateLimitService is shared, use different provider name for each test as workaround
        this.providerName = "zz-provider-" + UUID.randomUUID().toString();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenBuiltWithNullProviderName() {
        new RateLimitFilter(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenBuiltWithEmptyProviderName() {
        new RateLimitFilter("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenBuiltWithInvalidRetryTime() {
        new RateLimitFilter(providerName, 2000, 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenBuiltWithZeroRetryTime() {
        new RateLimitFilter(providerName, 0, 0);
    }

    @Test
    public void shouldInitWithProviderName() {
        new RateLimitFilter(providerName);
    }

    @Test
    public void shouldInitWithProviderNameAndRetryTime() {
        new RateLimitFilter(providerName, 1000, 2000);
    }

    @Test
    public void shouldNotifyRateLimiterAndFail() {
        final RateLimitFilter rateLimitFilter = new RateLimitFilter(providerName);
        rateLimitFilter.setNext(new MockResponseFilter(mockRateLimitResponse()));

        try {
            rateLimitFilter.handle(null);
            fail("This should not be reached - exception should be thrown");
        } catch (BankServiceException ex) {
            assertEquals(
                    "Should throw bank service exception",
                    BankServiceError.ACCESS_EXCEEDED,
                    ex.getError());
        }

        assertTrue(
                "Should notify rate limit service",
                RateLimitService.INSTANCE.hasReceivedRateLimitNotificationRecently(providerName));
    }

    @Test
    public void shouldNotifyRateLimiterAndRetry() {
        final RateLimitFilter rateLimitFilter = new RateLimitFilter(providerName, 1, 2);
        rateLimitFilter.setNext(new MockResponseFilter(mockRateLimitResponse(), mockOkResponse()));
        rateLimitFilter.handle(null);

        assertTrue(
                "Should notify rate limit service",
                RateLimitService.INSTANCE.hasReceivedRateLimitNotificationRecently(providerName));
    }

    @Test
    public void shouldNotNotifyRateLimiterAndRetry() {
        final RateLimitFilter rateLimitFilter = new RateLimitFilter(providerName);
        rateLimitFilter.setNext(new MockResponseFilter(mockOkResponse()));
        rateLimitFilter.handle(null);

        assertFalse(
                "Should not notify rate limit service",
                RateLimitService.INSTANCE.hasReceivedRateLimitNotificationRecently(providerName));
    }

    private HttpResponse mockRateLimitResponse() {
        final HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(429);
        when(response.getBody(String.class)).thenReturn("{\"error\":\"ACCESS_EXCEEDED\"}");
        return response;
    }

    private HttpResponse mockOkResponse() {
        final HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getBody(String.class)).thenReturn("[]");
        return response;
    }
}
