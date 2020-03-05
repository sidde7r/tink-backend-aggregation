package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AssertionEntity {

    private String assertionId;
    private Boolean historyEnabled;
    private Long lastUsed;
    private Integer length;
    private String method;
    private String status;

    public String getAssertionId() {
        return assertionId;
    }

    public Boolean getHistoryEnabled() {
        return historyEnabled;
    }

    public Long getLastUsed() {
        return lastUsed;
    }

    public Integer getLength() {
        return length;
    }

    public String getMethod() {
        return method;
    }

    public String getStatus() {
        return status;
    }
}
