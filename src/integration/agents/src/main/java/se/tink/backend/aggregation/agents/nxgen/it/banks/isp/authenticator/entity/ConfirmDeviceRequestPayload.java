package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.IspConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmDeviceRequestPayload {
    @JsonProperty private String clientId = IspConstants.Crypto.CLIENT_ID;
    @JsonProperty private String clientSecret = IspConstants.Crypto.CLIENT_SECRET;
    @JsonProperty private String deviceId;

    @JsonProperty("otp")
    private String totp;

    @JsonProperty("trxid")
    private String transactionId;

    public ConfirmDeviceRequestPayload(
            final String deviceId, final String totp, final String transactionId) {
        this.deviceId = Objects.requireNonNull(deviceId);
        this.totp = Objects.requireNonNull(totp);
        this.transactionId = Objects.requireNonNull(transactionId);
    }
}
