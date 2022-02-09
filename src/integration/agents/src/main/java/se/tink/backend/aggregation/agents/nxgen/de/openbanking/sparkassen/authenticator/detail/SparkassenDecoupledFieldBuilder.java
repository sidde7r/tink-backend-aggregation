package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.de.DecoupledFieldBuilder;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableParametrizedKey;

@RequiredArgsConstructor
public class SparkassenDecoupledFieldBuilder implements DecoupledFieldBuilder {

    private static final LocalizableParametrizedKey INSTRUCTIONS =
            new LocalizableParametrizedKey(
                    "Please open the S-pushTAN app on device \"{0}\" and confirm login. Then click the \"Submit\" button");

    private final Catalog catalog;

    @Override
    public List<Field> getInstructionsField(ScaMethodEntity scaMethod) {
        return Collections.singletonList(
                CommonFields.Instruction.build(
                        catalog.getString(INSTRUCTIONS, scaMethod.getName())));
    }
}
