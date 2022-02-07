package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.toggle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

@Slf4j
@RequiredArgsConstructor
public class UnicreditEmbeddedFlowToggle {

    private static final String FEATURE_TOGGLE_NAME = "de-unicredit-embedded-flow";
    private final UnleashClient client;

    public boolean isEnabled() {
        try {
            Toggle toggle = Toggle.of(FEATURE_TOGGLE_NAME).build();
            return client.isToggleEnabled(toggle);
        } catch (Exception e) {
            log.info("Unleash is not available, using default flow", e);
            return false;
        }
    }
}
