package se.tink.agent.sdk.authentication.authenticators.berlingroup;

import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Builder
@Getter
public class BerlinGroupConsent {
    private final String consentId;
    private final URL consentAppUrl;
}
