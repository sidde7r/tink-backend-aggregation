package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AnonymousInvokeBindResponseData {

    private Boolean assertionsComplete;
    private String challenge;
    private List<ControlFlowEntity> controlFlow;
    private String state;

    public Boolean getAssertionsComplete() {
        return assertionsComplete;
    }

    public String getChallenge() {
        return challenge;
    }

    public List<ControlFlowEntity> getControlFlow() {
        return controlFlow;
    }

    public String getState() {
        return state;
    }
}
