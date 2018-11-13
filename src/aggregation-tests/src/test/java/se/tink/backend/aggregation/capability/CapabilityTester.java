package se.tink.backend.aggregation.capability;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
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

    @Test
    public void testDemoAgentImplementsAllCapabilities() throws ClassNotFoundException {
        String className = "demo.DemoAgent";
        List<Capability> enums = Arrays.asList(Capability.values());
        Set<Capability> capabilities = CapabilityExtractor.extract(className);

        List<Capability> notPresentCapabilities = enums
                .stream()
                .filter(c -> !capabilities.contains(c))
                .collect(Collectors.toList());

        Assert.assertTrue(notPresentCapabilities.isEmpty());
    }

    @Test
    public void testCapabilitiesGetParsed() throws ClassNotFoundException {
        String className = "demo.DemoAgent";
        List<Capability> enums = Arrays.asList(Capability.values());
        Set<Capability> capabilities = CapabilityExtractor.extract(className);

        List<Capability> presentCapabilities =
                capabilities
                .stream()
                .filter(c -> enums.contains(c))
                .collect(Collectors.toList());

        Assert.assertTrue(capabilities.size() == presentCapabilities.size());
    }


}
