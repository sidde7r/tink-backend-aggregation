package se.tink.backend.aggregation.agents.framework.converter;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.List;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.S3LogFormatAdapter;

public class S3ToAAPFileConverter {

    public static void main(final String[] args) {
        String resourceFile = args[0];
        String targetFile = args[1];
        S3LogFormatAdapter adapter = new S3LogFormatAdapter();

        List<String> content =
                new S3LogFormatAdapter()
                        .toMockFileFormat(new ResourceFileReader().read(resourceFile));
        String output = String.join("\n", content);

        File outputFile = new File(targetFile);
        try {
            Files.write(output, outputFile, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
