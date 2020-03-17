package se.tink.libraries.retrypolicy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RetryContextTest {

    @Test
    public void newContextShouldHaveZeroAttemptsAndNullLastThrowable() {
        // given

        // when
        RetryContext retryContext = new RetryContext();

        // then
        assertThat(retryContext.getAttempt()).isEqualTo(0);
        assertThat(retryContext.getLastThrowable()).isNull();
    }

    @Test
    public void addingThrowablesToContextIncreasesAttempts() {
        // given
        RetryContext retryContext = new RetryContext();

        // when
        retryContext.setNewThrowable(new RuntimeException());
        retryContext.setNewThrowable(new IllegalStateException());
        retryContext.setNewThrowable(new IllegalArgumentException());

        // then
        assertThat(retryContext.getAttempt()).isEqualTo(3);
    }

    @Test
    public void contextReturnsLastAddedThrowable() {
        // given
        RetryContext retryContext = new RetryContext();

        // when
        retryContext.setNewThrowable(new RuntimeException());
        retryContext.setNewThrowable(new IllegalStateException());
        retryContext.setNewThrowable(new IllegalArgumentException());

        // then
        assertThat(retryContext.getLastThrowable()).isInstanceOf(IllegalArgumentException.class);
    }
}
