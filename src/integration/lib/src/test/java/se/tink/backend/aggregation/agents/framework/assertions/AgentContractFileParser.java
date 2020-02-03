package se.tink.backend.aggregation.agents.framework.assertions;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;

public class AgentContractFileParser {

    private static String readFile(String filePath) {
        String fileContent;
        try {
            fileContent =
                    new String(
                            Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileContent;
    }

    public static AgentContractEntity parseAgentContractFile(String filePath) {
        String content = readFile(filePath);
        try {
            return new ObjectMapper().readValue(content, AgentContractEntity.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
