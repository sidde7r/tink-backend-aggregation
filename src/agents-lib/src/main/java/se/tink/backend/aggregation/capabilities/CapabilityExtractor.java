package se.tink.backend.aggregation.capabilities;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.AgentClassFactory;
import se.tink.backend.aggregation.annotations.Implements;
import se.tink.backend.aggregation.constants.Capability;

public class CapabilityExtractor {
    private static Logger logger = LoggerFactory.getLogger(CapabilityExtractor.class);

    public static Set<Capability> extract(String providerClassName) throws ClassNotFoundException {
        Class className = AgentClassFactory.getAgentClass(providerClassName);
        Implements impl = (Implements) (className).getAnnotation(Implements.class);
        if (Objects.isNull(impl)) {
            logger.warn("Agent class: {} does not contain the Implements annotation", className.getSimpleName());
            return Collections.emptySet();
        }
        return Sets.newHashSet(impl.value());
    }
}
