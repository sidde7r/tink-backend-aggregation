package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.rpc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.ResponseErrorCodes;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorsEntity {
    private String id;
    private String code;
    private String title;
    private String technicalDescription;
    private DetailsEntity details;

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getTechnicalDescription() {
        return technicalDescription;
    }

    public DetailsEntity getDetails() {
        return details;
    }

    @JsonIgnore
    public boolean isChallengeError() {
        return isChallengeRequired() || isChallengeInvalid();
    }

    @JsonIgnore
    public boolean isChallengeRequired() {
        return ResponseErrorCodes.CHALLENGE_REQUIRED.equalsIgnoreCase(code);
    }

    @JsonIgnore
    public boolean isChallengeInvalid() {
        return ResponseErrorCodes.INVALID_CHALLENGE_VALUE.equalsIgnoreCase(code);
    }
}
