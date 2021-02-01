package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class TokenResponse {
    private String rememberMeToken;
    private String fullName;
    private String deleteKey;
    private String reactivateKey;
    private String obfuscatedSsn;
    private String bankKey;
    private String bankName;
}
