package se.tink.backend.insights.core.valueobjects;

import java.util.UUID;

public class InsightAction {
    private InsightActionId id;
    private InsightActionType type;
    private String description;
    private ButtonDivType buttonDivType;

    InsightAction(InsightActionType type, String description, ButtonDivType buttonDivType) {
        this.id = InsightActionId.of(UUID.randomUUID().toString());
        this.type = type;
        this.description = description;
        this.buttonDivType = buttonDivType;
    }

    public InsightActionId getId() {
        return id;
    }

    public InsightActionType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public ButtonDivType getButtonDivType() {
        return buttonDivType;
    }

    public static InsightAction of(InsightActionType type, String description, ButtonDivType buttonDivType) {
        return new InsightAction(type, description, buttonDivType);
    }
}
