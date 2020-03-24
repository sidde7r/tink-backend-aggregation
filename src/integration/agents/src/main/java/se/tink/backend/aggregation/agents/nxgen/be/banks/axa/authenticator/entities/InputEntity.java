package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class InputEntity {

    private String cardNbr;
    private String profileName;

    public InputEntity setCardNbr(String cardNbr) {
        this.cardNbr = cardNbr;
        return this;
    }

    public InputEntity setProfileName(String profileName) {
        this.profileName = profileName;
        return this;
    }
}
