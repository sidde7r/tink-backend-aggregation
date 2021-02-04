package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class UserTOCResponse {
    private String registrationState;
    private String registrationReason;
    private boolean registrationTocIsRequired;
    private boolean isRegistered;
    private boolean isInUttagsplanMalgrupp;
    private boolean harGodkantSenasteAnvandaravtal;

    @JsonProperty("anvandaravtalGodkannande")
    private boolean userAgreementApproved;
}
