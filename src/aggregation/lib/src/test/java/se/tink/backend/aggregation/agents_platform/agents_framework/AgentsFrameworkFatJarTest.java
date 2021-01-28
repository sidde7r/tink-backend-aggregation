package se.tink.backend.aggregation.agents_platform.agents_framework;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public final class AgentsFrameworkFatJarTest {

    private static final String AGENTS_FRAMEWORK_JAR_SUFFIX =
            "src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/agents_framework/agents_framework.jar";

    /**
     * If this test fails for you, this likely means that changes have recently been made to the
     * Agents Platform Agents Framework. The classes you see in the error output are part of a
     * third-party library that the Agents Framework now depends on. This third-party dependency
     * needs to be shaded, or else it is extremely likely that Aggregation service will end up with
     * third-party version conflicts on the classpath.
     *
     * <p>To shade the third-party library, open agents_framework/shade_rules.txt and add a "shading
     * rule" for the library based on its package name. This will relocate the library inside the
     * fat-jar, which you can find at:
     *
     * <pre>
     * bazel-bin/src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/agents_framework/agents_framework.jar
     * </pre>
     */
    @Test
    public void fatJarDoesNotContainUnexpectedUnshadedClasses() throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        URL[] classpathUrls = ((URLClassLoader) classLoader).getURLs();

        String jarPath =
                Stream.of(classpathUrls)
                        .map(URL::getFile)
                        .filter(s -> s.endsWith(AGENTS_FRAMEWORK_JAR_SUFFIX))
                        .findAny()
                        .get();

        JarFile jarFile = new JarFile(jarPath);
        List<JarEntry> jarEntries = Collections.list(jarFile.entries());

        List<JarEntry> unexpectedClassesInJar =
                jarEntries.stream()
                        .filter(AgentsFrameworkFatJarTest::entryIsUnexpected)
                        .collect(Collectors.toList());

        Assertions.assertThat(unexpectedClassesInJar).isEmpty();
    }

    private static boolean entryIsUnexpected(JarEntry jarEntry) {
        if (jarEntry.isDirectory()) {
            return false;
        }
        if (!jarEntry.getName().endsWith(".class")) {
            return false;
        }
        if (jarEntry.getName().equals("module-info.class")) {
            return false;
        }

        List<String> expectedPrefixes =
                Arrays.asList(
                        "se/tink/",
                        "com/tink/",
                        "agents_platform_agents_framework/",
                        // Permissible unshaded libraries for now
                        "ch/qos/logback/",
                        "com/fasterxml/",
                        "io/protostuff/",
                        "io/swagger/",
                        "javax/",
                        "junit/",
                        "net/jcip/",
                        "org/bouncycastle/",
                        "org/junit/",
                        "org/slf4j/",
                        "org/xnap/",
                        "redis/clients/");

        for (String prefix : expectedPrefixes) {
            if (jarEntry.getName().startsWith(prefix)) {
                return false;
            }
        }

        return true;
    }
}
