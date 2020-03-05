package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ControlFlowEntity {

    private String assertionId;
    private AppDataEntity appData;
    private String formId;
    private String type;
    private List<MethodEntity> methods;
    private Object options;
    private List<AssertionEntity> assertions;

    public String getAssertionId() {
        return assertionId;
    }

    public AppDataEntity getAppData() {
        return appData;
    }

    public String getFormId() {
        return formId;
    }

    public String getType() {
        return type;
    }

    public List<MethodEntity> getMethods() {
        return methods;
    }

    public Object getOptions() {
        return options;
    }

    public List<AssertionEntity> getAssertions() {
        return assertions;
    }
}
