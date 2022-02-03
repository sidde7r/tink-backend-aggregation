package se.tink.backend.aggregation.nxgen.propertiesloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import lombok.NonNull;

final class AgentPropertiesLoader {

    private static final String PROPERTIES_FILE_SUFFIX = "-prod-properties.yaml";
    private static final String RESOURCES_PACKAGE = "/resources";

    private final String agentClassPath;
    private final AgentPropertiesReader reader;
    private final AgentPropertiesScanner scanner;

    AgentPropertiesLoader(@NonNull String agentClassPath) {
        this.agentClassPath = agentClassPath;
        this.reader = new AgentPropertiesReader();
        this.scanner = new AgentPropertiesScanner();
    }

    public <T> T load(@NonNull Class<T> className) throws IOException {
        File propertiesFile =
                scanner.scan(agentClassPath + RESOURCES_PACKAGE, PROPERTIES_FILE_SUFFIX)
                        .orElseThrow(
                                () ->
                                        new FileNotFoundException(
                                                "Couldn't find properties file in the agent's resources package!"));
        return reader.read(propertiesFile, className);
    }
}
