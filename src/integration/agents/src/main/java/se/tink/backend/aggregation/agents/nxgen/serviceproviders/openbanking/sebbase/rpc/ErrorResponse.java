package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private String type;
    private String title;
    private String detail;
    private String code;

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public String getCode() {
        return code;
    }

    public boolean isPollTimeout() {
        return SebCommonConstants.PollResponses.TIMEOUT.equalsIgnoreCase(type);
    }
}
