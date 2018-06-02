package se.tink.backend.sms.otp.rpc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.UUID;

public class ConsumeRequest {
    private UUID otpId;

    public ConsumeRequest(String otpId) {
        Preconditions.checkState(!Strings.isNullOrEmpty(otpId));
        this.otpId = UUID.fromString(otpId);
    }

    public UUID getOtpId() {
        return otpId;
    }
}
