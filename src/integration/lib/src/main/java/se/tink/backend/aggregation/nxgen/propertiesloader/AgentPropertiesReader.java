package se.tink.backend.aggregation.nxgen.propertiesloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;

class AgentPropertiesReader {
    private final ObjectMapper objectMapper;

    AgentPropertiesReader() {
        objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.registerModule(new JavaTimeModule());
    }

    <T> T read(File propertiesFile, Class<T> className) throws IOException {
        return objectMapper.readValue(propertiesFile, className);
    }
}
