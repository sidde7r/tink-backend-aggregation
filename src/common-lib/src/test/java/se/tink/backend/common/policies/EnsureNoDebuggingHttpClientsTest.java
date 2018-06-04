package se.tink.backend.common.policies;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

/**
 * Ensures that we don't use the createProxyClient in any `/main/java/.*.java` file, since it's easily forgotten left
 * when developing which causes e.g. agents not to work.
 */
public class EnsureNoDebuggingHttpClientsTest {
    private static final String[] SEARCHED_FILE_EXTENSIONS = new String[] {"java"};
    private static final ImmutableList<String> EXCLUDED_FILE_NAMES = ImmutableList.of(
            "JerseyClientFactory.java",
            "AbstractJerseyClientFactory.java");

    /**
     * Soft assertions enables that we don't throw on first occurance, but instead run through all files and show all
     * assert fails at the end of the test method run.
     */
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void ensureNoProxyClientsAreUsedInAgents() throws IOException {
        assertModuleMainDirsRecursiveDoesNotContain("createProxyClient");
    }

    private void assertModuleMainDirsRecursiveDoesNotContain(String string) throws IOException {
        File srcRoot = new File(System.getProperty("user.dir") + "/src");
        List<File> moduleMainDirs = findDirectoriesEndingWith(srcRoot, "/src/main");

        for (File moduleMainDir : moduleMainDirs) {
            assertFilesRecursivelyDoesNotContain(moduleMainDir, string);
        }
    }

    private static List<File> findDirectoriesEndingWith(File root, String endingWith) {
        List<File> matches = Lists.newArrayList();
        findDirectoriesEndingWithRecursive(root, endingWith, matches);
        return matches;
    }

    private static void findDirectoriesEndingWithRecursive(File root, String endingWith, List<File> matches) {
        if (root == null) {
            return;
        }

        File[] subPaths = root.listFiles(IS_DIRECTORY);

        // Check if any direct sub paths matches the /main/java dir, then don't go through all dirs
        for (File subPath : subPaths) {
            if (subPath.getPath().endsWith(endingWith)) {
                matches.add(subPath);
                return;
            }
        }

        // Recurse all sub paths reasonable for finding other module main dirs
        for (File subPath : subPaths) {
            findDirectoriesEndingWithRecursive(subPath, endingWith, matches);
        }
    }

    private static final FileFilter IS_DIRECTORY = File::isDirectory;

    private void assertFilesRecursivelyDoesNotContain(File root, String string) throws IOException {
        Collection<File> javaFiles = FileUtils.listFiles(root, SEARCHED_FILE_EXTENSIONS, true);

        for (File javaFile : javaFiles) {
            if (EXCLUDED_FILE_NAMES.contains(javaFile.getName())) {
                continue;
            }

            assertFileDoesNotContain(javaFile, string);
        }
    }

    private void assertFileDoesNotContain(File file, String string) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath(), Charset.forName("UTF-8"));
        for (String line : lines) {
            softly.assertThat(line)
                    .withFailMessage("Found \"" + string + "\"" + " in " + file.getName())
                    .doesNotContain(string);
        }
    }
}
