package se.tink.backend.aggregation.agents.framework.converter;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.List;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;

public class S3ToAAPFileConverter {

    public static void main(final String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException(
                    "Is necessary specify as a first param the origin file with the path and as a second param the output path with a filename \nplease re-run the program with correct arguments: <origin> <destination>");
        }
        String resourceFile = args[0];
        String targetFile = args[1];

        List<String> content =
                new S3LogFormatAdapter()
                        .toMockFileFormat(new ResourceFileReader().read(resourceFile));
        String output = String.join("\n", content);

        File outputFile = new File(targetFile);
        try {
            Files.asCharSink(outputFile, Charsets.UTF_8).write(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
