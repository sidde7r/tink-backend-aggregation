package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FortisProcessState {

    private String deviceId;
    private String loginSessionId;
    private String oathTokenId;
    private String registrationCode;
    private String cardFrameId;

    private String encryptionKey;
    private String enrollmentSessionId;
    private String encCredentials;
    private String smsOtp;
}
