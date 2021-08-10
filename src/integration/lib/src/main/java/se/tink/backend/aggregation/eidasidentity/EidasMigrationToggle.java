package se.tink.backend.aggregation.eidasidentity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.finn.unleash.UnleashContext;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

@RequiredArgsConstructor
@Slf4j
public class EidasMigrationToggle {
    private static final String FEATURE_TOGGLE_NAME = "EidasMigration";

    private final UnleashClient unleashClient;

    public AvailableCertIds getEnabledCertId(String appId, String providerName) {
        Toggle toggle = createToggle(appId, providerName);
        try {
            final AvailableCertIds availableCertId =
                    unleashClient.isToggleEnable(toggle)
                            ? AvailableCertIds.OLD
                            : AvailableCertIds.DEFAULT;
            log.info("Enabled certId: {}", availableCertId.getValue());
            return availableCertId;
        } catch (Exception e) {
            log.error("Unleash is not available, use DEFAULT certificate");
            return AvailableCertIds.DEFAULT;
        }
    }

    private static Toggle createToggle(String appId, String providerName) {
        return Toggle.of(FEATURE_TOGGLE_NAME)
                .context(
                        UnleashContext.builder()
                                .addProperty("appId", appId)
                                .addProperty("providerName", providerName)
                                .build())
                .build();
    }
}
