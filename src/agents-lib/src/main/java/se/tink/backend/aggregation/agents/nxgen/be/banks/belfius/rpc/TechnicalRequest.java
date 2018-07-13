package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TechnicalRequest extends RequestEntity {

    private String applicationId;
    private String requestType;

    public String getApplicationId() {
        return applicationId;
    }

    public String getRequestType() {
        return requestType;
    }

    public static TechnicalRequest withRequestType(String requestType) {
        TechnicalRequest request = new TechnicalRequest();
        request.applicationId = BelfiusConstants.Request.APPLICATION_ID;
        request.requestType = requestType;
        return request;
    }
}
