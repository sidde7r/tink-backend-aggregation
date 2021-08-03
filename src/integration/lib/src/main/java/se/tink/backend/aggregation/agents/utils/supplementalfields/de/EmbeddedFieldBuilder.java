package se.tink.backend.aggregation.agents.utils.supplementalfields.de;

import java.util.List;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;

// Covers common cases of generating Fields used in supplemental info exchange in embedded setting.
public interface EmbeddedFieldBuilder {

    Field getChooseScaMethodField(List<ScaMethodEntity> scaMethods);

    List<Field> getOtpFields(ScaMethodEntity scaMethod, ChallengeDataEntity challengeData);
}
