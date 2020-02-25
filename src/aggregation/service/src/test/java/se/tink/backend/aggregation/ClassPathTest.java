package se.tink.backend.aggregation;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public final class ClassPathTest {

    private static final String GUAVA = "guava-\\d+\\.\\d+(-jre)?\\.jar";

    @Test
    public void noConflictingVersionsOfGuava() {
        URL[] urls = ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs();
        Set<String> guavaJars =
                Stream.of(urls)
                        .map(URL::toString)
                        .map(s -> s.split("/"))
                        .map(tokens -> tokens[tokens.length - 1])
                        .filter(s -> s.matches(GUAVA))
                        .collect(Collectors.toSet());

        assert guavaJars.size() <= 1 : String.format("Conflicting dependencies: %s", guavaJars);
    }
}
