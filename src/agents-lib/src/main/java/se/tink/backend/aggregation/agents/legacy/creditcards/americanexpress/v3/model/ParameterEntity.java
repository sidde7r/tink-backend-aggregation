package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

public class ParameterEntity {
    private String label;
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
