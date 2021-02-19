package se.tink.backend.aggregation.agents.utils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Ignore;

@Ignore
public class IsNot0Matcher extends BaseMatcher<Double> {
    private IsNot0Matcher() {}

    public static IsNot0Matcher isNot0() {
        return new IsNot0Matcher();
    }

    @Override
    public boolean matches(Object item) {
        return (double) item != 0;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("<!0>");
    }
}
