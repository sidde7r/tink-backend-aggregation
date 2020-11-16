package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import lombok.Value;

@Value
public class KbcFetchConsentExternalApiCallParameters {

    private String iban;
    private String redirectUrl;
    private String psuIpAddress;

    public String getIban() {
        return iban;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getPsuIpAddress() {
        return psuIpAddress;
    }
}
