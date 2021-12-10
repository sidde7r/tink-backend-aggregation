package se.tink.agent.sdk.authentication.authenticators.berlingroup;

import java.time.Period;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BerlinGroupAuthenticatorConfiguration {
    private final Period consentValidForPeriod;
    private final String consentIdStorageKey;
}
