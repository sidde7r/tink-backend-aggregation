package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AssertResponseData {

    private Integer assertionErrorCode;
    private String assertionErrorMessage;
    private Boolean assertionsComplete;
    private List<ControlFlowEntity> controlFlow;
    private ResponseDataEntity data;
    private String state;
    private String token;

    public Integer getAssertionErrorCode() {
        return assertionErrorCode;
    }

    public String getAssertionErrorMessage() {
        return assertionErrorMessage;
    }

    public Boolean getAssertionsComplete() {
        return assertionsComplete;
    }

    public List<ControlFlowEntity> getControlFlow() {
        return controlFlow;
    }

    public ResponseDataEntity getData() {
        return data;
    }

    public String getState() {
        return state;
    }

    public String getToken() {
        return token;
    }
}
