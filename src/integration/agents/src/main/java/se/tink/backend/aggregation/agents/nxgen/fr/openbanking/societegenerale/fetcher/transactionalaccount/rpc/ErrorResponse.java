package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private static final String GATEWAY_TIMEOUT_ERROR = "Gateway Timeout";
    private static final String NO_ACCESS_TO_ONLINE_BANKING_MESSAGE =
            "Online Service consultation not allowed for this customer";
    private String error;
    private String message;

    public boolean isGatewayTimeout() {
        return GATEWAY_TIMEOUT_ERROR.equals(error);
    }

    public boolean isNoAccessToMobileBanking() {
        return NO_ACCESS_TO_ONLINE_BANKING_MESSAGE.equals(message);
    }
}
