package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class KbcConsentValidationParameters {
    private String consentId;
    private String accessToken;
    private String psuIpAddress;
}
