package se.tink.backend.aggregation.agents.framework.assertions;

import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.assertions.utils.ResourceFileReader;

public class AgentContractEntitiesJsonFileParser {

    private final ResourceFileReader resourceFileReader;
    private final AgentContractEntitiesJsonParser agentContractEntitiesJsonParser;

    public AgentContractEntitiesJsonFileParser() {
        resourceFileReader = new ResourceFileReader();
        agentContractEntitiesJsonParser = new AgentContractEntitiesJsonParser();
    }

    public AgentContractEntity parseContractOnBasisOfFile(String filePath) {
        String fileContent = resourceFileReader.read(filePath);
        return agentContractEntitiesJsonParser.parse(fileContent);
    }
}
