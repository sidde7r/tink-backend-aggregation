package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ChannelEntity {

    private String assertionId;
    private String target;
    private String type;

    public String getAssertionId() {
        return assertionId;
    }

    public String getTarget() {
        return target;
    }

    public String getType() {
        return type;
    }
}
