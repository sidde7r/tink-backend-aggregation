package se.tink.backend.aggregation.utils.json.deserializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TestRootEntity {

    @JsonProperty("TestEntities")
    @JsonDeserialize(using = TestIdentifierMapDeserializerImpl.class)
    Map<String, TestResultEntity> entities;

    public TestRootEntity() {}

    public Map<String, TestResultEntity> getEntities() {
        return entities;
    }
}
