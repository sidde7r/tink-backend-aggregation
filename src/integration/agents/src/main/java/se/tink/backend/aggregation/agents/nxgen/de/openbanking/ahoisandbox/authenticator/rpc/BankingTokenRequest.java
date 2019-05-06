package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class BankingTokenRequest {

    private String installationId;
    private String nonce;
    private String timestamp;

    public BankingTokenRequest(String installationId, String nonce, String timestamp) {
        this.installationId = installationId;
        this.nonce = nonce;
        this.timestamp = timestamp;
    }

    @JsonIgnore
    public String toBase64Header() {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        SerializationUtils.serializeToString(this)
                                .getBytes(StandardCharsets.UTF_8));
    }
}
