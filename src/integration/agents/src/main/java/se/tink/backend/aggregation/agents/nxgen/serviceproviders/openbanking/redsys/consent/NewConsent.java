package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NewConsent {

    private String consentId;
    private URL scaRedirectUrl;

    public NewConsent(String consentId) {
        this.consentId = consentId;
    }

    public String getConsentId() {
        return consentId;
    }

    public Optional<URL> getScaRedirectUrl() {
        return Optional.ofNullable(scaRedirectUrl);
    }

    public void setScaRedirectUrl(URL scaRedirectUrl) {
        this.scaRedirectUrl = scaRedirectUrl;
    }
}
