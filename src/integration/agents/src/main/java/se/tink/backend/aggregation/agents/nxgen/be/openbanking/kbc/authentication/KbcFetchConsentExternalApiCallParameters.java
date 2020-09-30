package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import java.util.Objects;

public class KbcFetchConsentExternalApiCallParameters {

    private String iban;
    private String redirectUrl;
    private String psuIpAddress;

    public KbcFetchConsentExternalApiCallParameters(
            String iban, String redirectUrl, String psuIpAddress) {
        this.iban = iban;
        this.redirectUrl = redirectUrl;
        this.psuIpAddress = psuIpAddress;
    }

    public String getIban() {
        return iban;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getPsuIpAddress() {
        return psuIpAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KbcFetchConsentExternalApiCallParameters that =
                (KbcFetchConsentExternalApiCallParameters) o;
        return Objects.equals(iban, that.iban)
                && Objects.equals(redirectUrl, that.redirectUrl)
                && Objects.equals(psuIpAddress, that.psuIpAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iban, redirectUrl, psuIpAddress);
    }
}
