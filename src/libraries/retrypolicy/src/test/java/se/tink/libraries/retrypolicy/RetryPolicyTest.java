package se.tink.libraries.retrypolicy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class RetryPolicyTest {

    private RetryPolicy retryPolicy;

    @Before
    public void setUp() {
        retryPolicy =
                new RetryPolicy(3, IllegalArgumentException.class, IllegalStateException.class);
    }

    @Test
    public void canRetryWhenContextAttemptsLessThanMaxAndThrowableInNull() {
        // given
        retryPolicy = new RetryPolicy(3, IllegalArgumentException.class);
        // and
        RetryContext context = new RetryContext();
        context.setNewThrowable(null);

        // when
        boolean result = retryPolicy.canRetry(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void canRetryWhenContextAttemptsLessThanMaxAndThrowableInRetryables() {
        // given
        retryPolicy =
                new RetryPolicy(3, IllegalArgumentException.class, IllegalStateException.class);
        // and
        RetryContext context = new RetryContext();
        context.setNewThrowable(new IllegalArgumentException());

        // when
        boolean result = retryPolicy.canRetry(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void canRetryWhenContextAttemptsLessThanMaxAndThrowableDerivedFromRetryables() {
        // given
        retryPolicy = new RetryPolicy(3, RuntimeException.class);
        // and
        RetryContext context = new RetryContext();
        context.setNewThrowable(new IllegalArgumentException());

        // when
        boolean result = retryPolicy.canRetry(context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void cantRetryWhenThrowableIsParentOfRetryable() {
        // given
        retryPolicy = new RetryPolicy(3, IllegalStateException.class);
        // and
        RetryContext context = new RetryContext();
        context.setNewThrowable(new RuntimeException());

        // when
        boolean result = retryPolicy.canRetry(context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void cantRetryWhenMaxAttemptsExceed() {
        // given
        int maxAttempts = 3;
        retryPolicy = new RetryPolicy(maxAttempts, IllegalStateException.class);
        // and
        RetryContext context = new RetryContext();
        for (int i = 0; i < maxAttempts; i++) {

            context.setNewThrowable(new IllegalStateException());
        }

        // when
        boolean result = retryPolicy.canRetry(context);

        // then
        assertThat(result).isFalse();
    }
}
