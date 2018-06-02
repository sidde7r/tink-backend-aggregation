package se.tink.backend.insights.core.valueobjects;

public enum ButtonDivType {
    BUTTON("button"),
    BUTTON_PRIMARY(String.format("%s primary", BUTTON.getValue())),
    BUTTON_PRIMARY_SUGGESTED(String.format("%s suggested", BUTTON_PRIMARY.getValue()));

    private final String value;
    ButtonDivType(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
