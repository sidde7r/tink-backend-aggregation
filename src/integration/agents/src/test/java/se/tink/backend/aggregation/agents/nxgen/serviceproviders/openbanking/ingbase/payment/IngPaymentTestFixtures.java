package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import java.lang.annotation.Annotation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;

@Ignore
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IngPaymentTestFixtures {

    static AgentPisCapability getAgentPisCapability(PisCapability pisCapability) {
        return new AgentPisCapability() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return AgentPisCapability.class;
            }

            @Override
            public PisCapability[] capabilities() {
                return new PisCapability[] {pisCapability};
            }

            @Override
            public String[] markets() {
                return new String[0];
            }
        };
    }
}
