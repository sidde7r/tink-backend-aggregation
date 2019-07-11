package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ComponentsEntity {
    private String headingText;
    private Object icon;
    private String type;
    private List<RowsEntity> rows;
    private List<ButtonsEntity> buttons;
    private Object style;
    private String formId;
    private String formKey;
    private String formValue;
    private ActionEntity action;

    public String getFormId() {
        return formId;
    }

    public String getFormValue() {
        return formValue;
    }
}
