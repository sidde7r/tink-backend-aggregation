package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.de.DecoupledFieldBuilder;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableParametrizedKey;

@RequiredArgsConstructor
public class SparkassenDecoupledFieldBuilder implements DecoupledFieldBuilder {

    private static final LocalizableParametrizedKey INSTRUCTIONS =
            new LocalizableParametrizedKey(
                    "Please open the S-pushTAN app on device \"{0}\" and confirm login. Then click the \"Submit\" button");

    private final Catalog catalog;

    @Override
    public Field getInstructionsField(ScaMethodEntity scaMethod) {
        return CommonFields.Instruction.build(catalog.getString(INSTRUCTIONS, scaMethod.getName()));
    }
}
