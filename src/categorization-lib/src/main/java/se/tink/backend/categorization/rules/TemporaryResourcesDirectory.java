package se.tink.backend.categorization.rules;

import com.google.common.base.Preconditions;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;

/**
 * Helper class to load files in JAR resources from file system.
 */
class TemporaryResourcesDirectory implements Closeable {

    private final Path tempDirectory;

    private TemporaryResourcesDirectory(Path tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public static TemporaryResourcesDirectory create(String directoryPrefix, String... resources) throws IOException {
        Path tempDirectory = Files.createTempDirectory(directoryPrefix);

        for (String resource : resources) {
            InputStream resourceAsStream = TemporaryResourcesDirectory.class.getResourceAsStream(resource);
            Preconditions.checkArgument(resourceAsStream != null, "Resource not found: " + resource);
            try {
                Files.copy(
                        resourceAsStream,
                        FileSystems.getDefault()
                                .getPath(tempDirectory.toAbsolutePath().toString(),
                                        new java.io.File(resource).getName())
                );
            } finally {
                resourceAsStream.close();
            }
        }

        return new TemporaryResourcesDirectory(tempDirectory);
    }

    @Override
    public void close() throws IOException {
        FileUtils.deleteDirectory(tempDirectory.toFile());
    }

    public File getFile() {
        return tempDirectory.toAbsolutePath().toFile();
    }
}
