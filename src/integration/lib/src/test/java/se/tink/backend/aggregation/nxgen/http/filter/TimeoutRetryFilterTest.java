package se.tink.backend.aggregation.nxgen.http.filter;

import java.net.SocketTimeoutException;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;

public class TimeoutRetryFilterTest {

    @Test(expected = IllegalArgumentException.class)
    public void ensureInit_withZeroRetryCount_throwsException() {
        new TimeoutRetryFilter(0, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureInit_withNegativeRetryCount_throwsException() {
        new TimeoutRetryFilter(-1, 100);
    }

    @Test
    public void ensureInit_withPositiveRetryCount_isOk() {
        new TimeoutRetryFilter(1, 100);
    }

    @Test
    public void ensureInit_withZeroSleep_isOk() {
        new TimeoutRetryFilter(1, 0);
    }

    @Test
    public void ensureInit_withPositiveSleep_isOk() {
        new TimeoutRetryFilter(1, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureInit_withNegativeSleep_throwsException() {
        new TimeoutRetryFilter(1, -100);
    }

    @Test
    public void ensureRetryFilter_withRetryOfThree_throwsExceptionWithFourAttempts_ifNoSuccess() {
        MockFinalFilter finalFilter = new MockFinalFilter(10);

        TimeoutRetryFilter retryFilter = new TimeoutRetryFilter(3, 0);
        retryFilter.setNext(finalFilter);

        try {
            retryFilter.handle(null);
        } catch (HttpClientException e) {

            Assert.assertTrue(e.getCause() instanceof SocketTimeoutException);
        }

        Assert.assertEquals(4, finalFilter.getCallCount());
    }

    @Test
    public void ensureRetryFilter_returnsWithOneAttempt_ifImmediateSuccess() {
        MockFinalFilter finalFilter = new MockFinalFilter(0);

        TimeoutRetryFilter retryFilter = new TimeoutRetryFilter(3, 0);
        retryFilter.setNext(finalFilter);

        retryFilter.handle(null);
        Assert.assertEquals(1, finalFilter.getCallCount());
    }

    @Test
    public void ensureRetryFilter_returnsWithThreeAttempts_ifSuccessAfterThree() {
        MockFinalFilter finalFilter = new MockFinalFilter(3);

        TimeoutRetryFilter retryFilter = new TimeoutRetryFilter(3, 0);
        retryFilter.setNext(finalFilter);

        retryFilter.handle(null);
        Assert.assertEquals(3, finalFilter.getCallCount());
    }
}
