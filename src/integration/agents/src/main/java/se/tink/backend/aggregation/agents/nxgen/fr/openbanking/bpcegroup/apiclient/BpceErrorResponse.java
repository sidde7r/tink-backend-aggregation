package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BpceErrorResponse {
    @JsonIgnore private static final String NOT_IMPLEMENTED = "NIMP";
    @JsonIgnore private static final String INTERNAL_ERROR = "INTE";
    @JsonIgnore private static final String TOO_MANY_REQUESTS = "TMRQ";
    @JsonIgnore private static final String NO_AVAILABLE_ACCOUNTS = "NAAC";

    private String message;
    private String path;

    public boolean isNotImplemented() {
        return NOT_IMPLEMENTED.equals(message);
    }

    public boolean isInternalError() {
        return INTERNAL_ERROR.equals(message);
    }

    public boolean isTooManyRequest() {
        return TOO_MANY_REQUESTS.equals(message);
    }

    public boolean isNoAvailableAccounts() {
        return NO_AVAILABLE_ACCOUNTS.equals(message);
    }
}
