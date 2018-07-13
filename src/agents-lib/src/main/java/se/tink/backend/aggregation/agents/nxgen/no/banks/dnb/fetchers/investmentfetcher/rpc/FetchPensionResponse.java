package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.entities.PensionDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchPensionResponse {
    private int errorCode;
    private String statusCode;
    private boolean success;
    private PensionDataEntity data;

    public int getErrorCode() {
        return errorCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public boolean getSuccess() {
        return success;
    }

    public PensionDataEntity getData() {
        return data;
    }
}
