package se.tink.backend.aggregation.eidasidentity;

import lombok.RequiredArgsConstructor;
import no.finn.unleash.UnleashContext;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

@RequiredArgsConstructor
public class EidasMigrationToggle {
    private static final String FEATURE_TOGGLE_NAME = "EidasMigration-";

    private final UnleashClient unleashClient;

    public AvailableCertIds getEnabledCertId(String marketCode, String providerName) {
        Toggle toggle = createToggle(marketCode, providerName);
        return unleashClient.isToggleEnable(toggle)
                ? AvailableCertIds.OLD
                : AvailableCertIds.DEFAULT;
    }

    private static Toggle createToggle(String marketCode, String providerName) {
        return Toggle.of(FEATURE_TOGGLE_NAME.concat(marketCode.toUpperCase()))
                .context(UnleashContext.builder().addProperty("providerName", providerName).build())
                .build();
    }
}
