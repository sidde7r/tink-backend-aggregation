package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.oauth.rpc;

import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.oauth.entities.DataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitMyWealthResponse {
    private int errorCode;
    private String statusCode;
    private boolean success;
    private DataEntity data;

    public int getErrorCode() {
        return errorCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public boolean getSuccess() {
        return success;
    }

    public DataEntity getData() {
        return data;
    }
}
