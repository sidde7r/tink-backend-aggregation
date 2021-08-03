package se.tink.backend.aggregation.agents.utils.supplementalfields.de;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;

// Covers common cases of generating Fields used in supplemental info exchange in embedded setting.
public interface DecoupledFieldBuilder {

    Field getInstructionsField(ScaMethodEntity scaMethod);
}
