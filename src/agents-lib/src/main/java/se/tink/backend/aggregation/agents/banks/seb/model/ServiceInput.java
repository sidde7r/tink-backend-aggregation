package se.tink.backend.aggregation.agents.banks.seb.model;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceInput {

    @JsonProperty("Condition")
    public String Condition = "EQ";
    @JsonProperty("VariableName")
    public String VariableName;
    @JsonProperty("VariableValue")
    public Object VariableValue;

    public ServiceInput(String name, String value) {
        VariableName = name;
        VariableValue = value;
    }

    public ServiceInput(String name, Integer value) {
        VariableName = name;
        VariableValue = value;
    }
}
