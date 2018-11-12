package se.tink.backend.aggregation.capability;

import java.util.Collections;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.capabilities.CapabilityExtractor;
import se.tink.backend.aggregation.constants.Capability;

public class CapabilityTester {
    private static final Logger log = LoggerFactory.getLogger(CapabilityTester.class);

    public static void checkCapabilities(String className) {

        Set<Capability> capablities = Collections.emptySet();
        try {
            capablities = CapabilityExtractor.extract(className);
        } catch (ClassNotFoundException e) {
            log.warn("Agent class not found "+ className +".");
        }

        if (capablities.isEmpty()) {
            log.warn("No capabilities found for agent "+className +".");
        }
    }
}
