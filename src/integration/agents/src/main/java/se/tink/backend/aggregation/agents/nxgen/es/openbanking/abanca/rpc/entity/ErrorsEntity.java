package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.rpc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
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
    public boolean challengeRequired() {
        if (Strings.isNullOrEmpty(code)) {
            return false;
        }
        if (code.trim().equalsIgnoreCase(ResponseErrorCodes.CHALLENGE_REQUIRED)
                || code.trim().equalsIgnoreCase(ResponseErrorCodes.INVALID_CHALLENGE_VALUE)) {
            return true;
        }
        return false;
    }
}
