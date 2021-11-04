package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LaBanquePostaleProxyErrorResponse {
    private static final String CONNECTION_RESET = "Connection reset";

    private boolean proxyError;
    private String errorText;

    public boolean isProxyError() {
        return proxyError && CONNECTION_RESET.equals(errorText);
    }
}
