package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ServiceInput implements RequestComponent {

    @JsonProperty("Condition")
    private String condition = "EQ";

    @JsonProperty("VariableName")
    private String variableName;

    @JsonProperty("VariableValue")
    private Object variableValue;

    public ServiceInput(String name, String value) {
        variableName = name;
        variableValue = value;
    }

    public ServiceInput(String name, Integer value) {
        variableName = name;
        variableValue = value;
    }
}
