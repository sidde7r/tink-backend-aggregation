package se.tink.libraries.http.client.masker;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;

public class SensitiveDataMaskerTest {

    final String sensitiveDataFilePath =
            "src/libraries/http_client/src/test/java/se/tink/libraries/http/client/masker/resources/sensitive.json";

    @Test
    public void shouldHashSensitiveData() throws IOException {
        String sensitiveString =
                new String(
                        Files.readAllBytes(Paths.get(sensitiveDataFilePath)),
                        StandardCharsets.UTF_8.name());
        String result = SensitiveDataMasker.mask(sensitiveString);
        assertThat(result).doesNotContain("sensitive");
    }
}
