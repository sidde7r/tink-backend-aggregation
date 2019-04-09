package se.tink.libraries.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Predicate;
import org.junit.Test;

public class AuthorizationHeaderPredicateTest {

    private static final Predicate<String> trueForAllPredicate = s -> true;

    @Test
    public void testShouldThrowExceptionIfNullMethod() {

        try {
            new AuthorizationHeaderPredicate(null, null);
        } catch (Exception e) {
            assertThat(e).hasMessage("Authorization method needs to be specified");
        }
    }

    @Test
    public void testShouldThrowExceptionIfEmptyMethod() {

        try {
            new AuthorizationHeaderPredicate("", null);
        } catch (Exception e) {
            assertThat(e).hasMessage("Authorization method needs to be specified");
        }
    }

    @Test
    public void testShouldMatchTwoParts() {

        AuthorizationHeaderPredicate predicate =
                new AuthorizationHeaderPredicate("my-method", trueForAllPredicate);

        assertThat(predicate.apply("my-method")).isFalse();
        assertThat(predicate.apply("my-method abc")).isTrue();

        // This is true since we limit the split of method and payload on the first space
        assertThat(predicate.apply("my-method abc abc")).isTrue();
    }

    @Test
    public void testShouldMatchMethod() {

        AuthorizationHeaderPredicate predicate =
                new AuthorizationHeaderPredicate("my-method", trueForAllPredicate);

        assertThat(predicate.apply("my-method abc")).isTrue();
        assertThat(predicate.apply("another-method abc")).isFalse();
    }

    @Test
    public void testShouldIgnoreCaseOnMethod() {

        AuthorizationHeaderPredicate predicate =
                new AuthorizationHeaderPredicate("token", trueForAllPredicate);

        assertThat(predicate.apply("token abc")).isTrue();
        assertThat(predicate.apply("TOKEN abc")).isTrue();
    }
}
