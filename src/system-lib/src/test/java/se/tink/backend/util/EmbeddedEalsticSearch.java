package se.tink.backend.util;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;

public class EmbeddedEalsticSearch extends ExternalResource {

    private Node node;
    private Path dataDirectory;

    @Override
    protected void before() throws Throwable {
        try {
            dataDirectory = Files.createTempDirectory("es-test", new FileAttribute[]{});
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        ImmutableSettings.Builder elasticsearchSettings = ImmutableSettings.settingsBuilder()
                .put("http.enabled", "false")
                .put("path.data", dataDirectory.toString());

        node = NodeBuilder.nodeBuilder()
                .local(true)
                .settings(elasticsearchSettings.build())
                .node();
    }

    @Override
    protected void after() {
        node.close();
        try {
            delete(dataDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public Client getClient() {
        return node.client();
    }

    private void delete(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file,
                                             BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
