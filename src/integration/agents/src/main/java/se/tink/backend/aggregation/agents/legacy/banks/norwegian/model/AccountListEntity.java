package se.tink.backend.aggregation.agents.banks.norwegian.model;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountListEntity {
    private boolean disabled;
    private boolean selected;
    private String text;
    private String value;

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
}
