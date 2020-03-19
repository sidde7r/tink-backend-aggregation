package se.tink.libraries.retrypolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class RetryExecutorTest {

    private static final int MAX_ATTEMPTS = 3;

    private RetryExecutor retryExecutor;

    @Before
    public void setUp() {
        RetryPolicy retryPolicy = new RetryPolicy(MAX_ATTEMPTS, IllegalStateException.class);
        retryExecutor = new RetryExecutor();
        retryExecutor.setRetryPolicy(retryPolicy);
    }

    @Test
    public void executeCallbackShouldEndSuccessfully() {
        // given
        String expectedResponse = "sample expected response";
        // and
        @SuppressWarnings("unchecked")
        RetryCallback<String, RuntimeException> retryCallback =
                (RetryCallback<String, RuntimeException>) mock(RetryCallback.class);
        given(retryCallback.retry()).willReturn(expectedResponse);

        // when
        String result = retryExecutor.execute(retryCallback);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(retryCallback).retry();
    }

    @Test
    public void executeCallbackShouldEndSuccessfullyIfCallbackFailsLessThanMaxAttemptsInPolicy() {
        // given
        String expectedResponse = "sample expected response";
        // and
        @SuppressWarnings("unchecked")
        RetryCallback<String, RuntimeException> retryCallback =
                (RetryCallback<String, RuntimeException>) mock(RetryCallback.class);
        given(retryCallback.retry())
                .willThrow(IllegalStateException.class)
                .willThrow(IllegalStateException.class)
                .willReturn(expectedResponse);

        // when
        String result = retryExecutor.execute(retryCallback);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(retryCallback, times(3)).retry();
    }

    @Test
    public void executeCallbackShouldFailWhenCallbackExceedsNumberOfMaxAttempts() {
        // given
        @SuppressWarnings("unchecked")
        RetryCallback<String, RuntimeException> retryCallback =
                (RetryCallback<String, RuntimeException>) mock(RetryCallback.class);
        given(retryCallback.retry())
                .willThrow(IllegalStateException.class)
                .willThrow(IllegalStateException.class)
                .willThrow(IllegalStateException.class);

        // when
        Throwable t = Assertions.catchThrowable(() -> retryExecutor.execute(retryCallback));

        // then
        assertThat(t).isInstanceOf(IllegalStateException.class);
        verify(retryCallback, times(3)).retry();
    }

    @Test
    public void executeCallbackShouldFailImmediatelyWhenCallbackThrowsNotRetryableException() {
        // given
        @SuppressWarnings("unchecked")
        RetryCallback<String, RuntimeException> retryCallback =
                (RetryCallback<String, RuntimeException>) mock(RetryCallback.class);
        given(retryCallback.retry()).willThrow(IllegalArgumentException.class);

        // when
        Throwable t = Assertions.catchThrowable(() -> retryExecutor.execute(retryCallback));

        // then
        assertThat(t).isInstanceOf(IllegalArgumentException.class);
        verify(retryCallback, times(1)).retry();
    }
}
