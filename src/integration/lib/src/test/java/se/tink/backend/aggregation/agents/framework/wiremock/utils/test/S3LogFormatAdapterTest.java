package se.tink.backend.aggregation.agents.framework.wiremock.utils.test;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.S3LogFormatAdapter;

public final class S3LogFormatAdapterTest {

    @Test
    public void ensureCorrectFormat() {
        // Given
        final String s3LogExamplePath =
                "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/wiremock/utils/test/resources/Barclays_001_S3LogFormatExample.txt";
        final String expectedFormatPath =
                "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/wiremock/utils/test/resources/Barclays_001_NewLogFormatExample.txt";

        final S3LogFormatAdapter adapter = new S3LogFormatAdapter();
        final String s3ExampleFileContent = new ResourceFileReader().read(s3LogExamplePath);
        final String expectedFormatContent = new ResourceFileReader().read(expectedFormatPath);

        // When (Also formats the list as a string incl, newlines in order to compare)
        final String readableLogOutput =
                adapter.toMockFileFormat(s3ExampleFileContent).stream()
                        .reduce((s1, s2) -> s1 + "\n" + s2)
                        .orElseThrow(IllegalStateException::new);

        // Then
        Assert.assertEquals(expectedFormatContent, readableLogOutput);
    }
}
