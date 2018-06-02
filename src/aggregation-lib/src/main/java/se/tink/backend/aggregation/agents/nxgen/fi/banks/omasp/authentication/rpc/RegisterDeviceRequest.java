package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.entities.SecurityKeyRequestEntity;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDeviceRequest {
    private String timestamp;
    @JsonProperty("mac")
    private String hmac;
    private SecurityKeyRequestEntity securityKey = new SecurityKeyRequestEntity();

    public RegisterDeviceRequest() {
        timestamp = LocalDateTime.now().format(OmaspConstants.TIMESTAMP_FORMATTER);
        hmac = Hash.hmacSha1AsHex(OmaspConstants.HMAC_KEY.getBytes(), timestamp.getBytes());
    }

    public RegisterDeviceRequest setSecurityKeyIndex(String securityKeyIndex) {
        securityKey.setSecurityKeyIndex(securityKeyIndex);
        return this;
    }

    public RegisterDeviceRequest setCardId(String cardId) {
        securityKey.setCardId(cardId);
        return this;
    }

    public RegisterDeviceRequest setSecurityCode(String securityCode) {
        securityKey.setSecurityCode(securityCode);
        return this;
    }
}
