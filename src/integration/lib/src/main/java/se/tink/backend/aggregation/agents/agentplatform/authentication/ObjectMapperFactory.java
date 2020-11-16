package se.tink.backend.aggregation.agents.agentplatform.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperFactory {

    private static final ObjectMapper OBJECT_MAPPER_SINGLETON = new ObjectMapper();

    public ObjectMapper getInstance() {
        return OBJECT_MAPPER_SINGLETON;
    }
}
