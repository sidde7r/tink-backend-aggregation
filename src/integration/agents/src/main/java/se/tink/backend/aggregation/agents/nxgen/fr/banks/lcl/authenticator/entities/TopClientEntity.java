package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TopClientEntity {
    @JsonProperty("banquePrivee")
    private boolean privateBank;

    @JsonProperty("topCLP")
    private boolean topclp;

    private boolean topMif;
    private boolean isBourse;
    private boolean topDemat;

    public boolean isPrivateBank() {
        return privateBank;
    }

    public boolean isTopclp() {
        return topclp;
    }

    public boolean isTopMif() {
        return topMif;
    }

    public boolean isBourse() {
        return isBourse;
    }

    public boolean isTopDemat() {
        return topDemat;
    }
}
