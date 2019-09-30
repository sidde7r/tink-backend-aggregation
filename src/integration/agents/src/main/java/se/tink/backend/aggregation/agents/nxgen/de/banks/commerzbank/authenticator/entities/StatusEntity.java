package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Values;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StatusEntity {
    private String status;
    private String approval;
    private String loginStatus;

    @JsonIgnore
    public boolean isStatusOk() {
        return Values.OK.equalsIgnoreCase(status);
    }

    @JsonIgnore
    public boolean isApprovalOk() {
        return Values.OK.equalsIgnoreCase(approval);
    }

    @JsonIgnore
    public boolean isLoginStatusOk() {
        return Values.OK.equalsIgnoreCase(loginStatus);
    }
}
