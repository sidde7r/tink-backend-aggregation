package se.tink.backend.aggregation;

import com.google.common.collect.ImmutableSet;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.description.Description;
import org.junit.Test;

public final class VersionConflictTest {

    // These are tolerated for now because they are hard to resolve
    private final ImmutableSet<String> toleratedConflicts =
            ImmutableSet.of(
                    "annotations",
                    "commons-codec",
                    "reflections",
                    "commons-logging",
                    "stax2-api",
                    "jetty-server",
                    "asm",
                    "commons-lang3",
                    "jetty-io",
                    "jetty-util",
                    "jetty-http",
                    "log4j");

    /**
     * If this test is failing for you, that likely means that you made some changes that introduced
     * a different version of a third-party library on the classpath. For example, up until now, we
     * might only have had "guava-25.1-jre.jar" on the classpath. But with your changes, maybe you
     * introduced a dependency on the "guava-23.0-jre.jar" jar as well. Having two conflicting
     * versions of a dependency on the classpath is dangerous as it can lead to NoSuchMethod
     * exceptions and confusing behavior.
     *
     * <p>Try to think of a solution that avoids introducing any version conflicts. Reach out to the
     * author of this test if you are unsure how to tackle this.
     */
    @Test
    public void noConflictingVersionsOfThirdPartyLibrariesOnClasspath() throws IOException {
        List<String> paths = getClasspathJars();

        Set<JarEntry> entries =
                paths.stream()
                        .map(JarEntry::pathToEntry)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());

        Set<String> names =
                entries.stream().map(e -> e.nameWithoutVersion).collect(Collectors.toSet());

        Map<String, ConflictInfo> allConflictInfos =
                names.stream()
                        .map(n -> ConflictInfo.from(n, entries))
                        .collect(Collectors.toMap(c -> c.name, c -> c));

        Map<String, ConflictInfo> conflictingInfos =
                allConflictInfos.entrySet().stream()
                        .filter(c -> c.getValue().versions.size() >= 2)
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        Set<String> newlyIntroducedVersionConflicts = conflictingInfos.keySet();
        newlyIntroducedVersionConflicts.removeAll(toleratedConflicts);

        // Tell the developer that their changes introduced a version conflict
        Description description =
                new Description() {
                    @Override
                    public String value() {
                        String anyConflictingLib =
                                newlyIntroducedVersionConflicts.iterator().next();
                        Set<String> versions = conflictingInfos.get(anyConflictingLib).versions;
                        Set<String> paths = conflictingInfos.get(anyConflictingLib).paths;
                        return String.format(
                                "There are conflicting versions of the library \"%s\" on the classpath: (%s) originating from: %s",
                                anyConflictingLib,
                                String.join(", ", versions),
                                String.join(", ", paths));
                    }
                };

        Assertions.assertThat(newlyIntroducedVersionConflicts).as(description).isEmpty();
    }

    private static List<String> getClasspathJars() throws IOException {
        String path = System.getProperty("java.class.path");

        String classPath;

        try (JarFile jarFile = new JarFile(path)) {
            // This would mean that classpath consists of a single manifest.mf file which in turn
            // contains a list of all the jars
            final Manifest manifest = jarFile.getManifest();
            classPath = manifest.getMainAttributes().getValue(Name.CLASS_PATH);
        } catch (FileNotFoundException e) {
            // This would mean that the classpath consists of all the jars, colon-separated
            classPath = path;
        }

        return Arrays.asList(classPath.split(":"));
    }

    private static class JarEntry {

        private String baseName;
        private String version;
        private String nameWithoutVersion;
        private String path;

        private static Optional<JarEntry> pathToEntry(final String jarPath) {
            if (!jarPath.contains("v1/http")) {
                return Optional.empty();
            }
            final JarEntry entry = new JarEntry();
            entry.baseName = FilenameUtils.getBaseName(jarPath);
            final List<String> parts = Arrays.asList(jarPath.split("/"));
            entry.version = parts.get(parts.size() - 2);
            entry.nameWithoutVersion = entry.baseName.replace("-" + entry.version, "");
            entry.path = jarPath;
            return Optional.of(entry);
        }
    }

    private static class ConflictInfo {

        private String name;
        private Set<String> versions;
        private Set<String> paths;

        private ConflictInfo(String name, Set<String> versions, Set<String> paths) {
            this.name = name;
            this.versions = versions;
            this.paths = paths;
        }

        private static ConflictInfo from(final String name, final Set<JarEntry> entries) {
            final Set<String> versions =
                    entries.stream()
                            .filter(e -> Objects.equals(e.nameWithoutVersion, name))
                            .map(e -> e.version)
                            .collect(Collectors.toSet());
            final Set<String> paths =
                    entries.stream()
                            .filter(e -> Objects.equals(e.nameWithoutVersion, name))
                            .map(e -> e.path)
                            .collect(Collectors.toSet());
            return new ConflictInfo(name, versions, paths);
        }
    }
}
