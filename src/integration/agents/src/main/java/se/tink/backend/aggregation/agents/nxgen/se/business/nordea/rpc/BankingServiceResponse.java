package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankingServiceResponse {
    public ErrorMessage errorMessage;

    public String getErrorCode() {
        return errorMessage.getErrorCode();
    }
}
