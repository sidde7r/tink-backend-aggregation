package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private static final String GATEWAY_TIMEOUT_ERROR = "Gateway Timeout";
    private String error;
    private String message;

    public boolean isGatewayTimeout() {
        return GATEWAY_TIMEOUT_ERROR.equals(error);
    }
}
