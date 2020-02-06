package se.tink.backend.aggregation.agents.framework.assertions;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;

public class AgentContractEntitiesJsonParser {

    public AgentContractEntity parse(String jsonContent) {
        try {
            return new ObjectMapper().readValue(jsonContent, AgentContractEntity.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
