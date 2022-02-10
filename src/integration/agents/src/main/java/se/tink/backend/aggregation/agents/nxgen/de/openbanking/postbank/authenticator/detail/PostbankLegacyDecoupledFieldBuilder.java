package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.detail;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.PostbankConstants;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.de.DecoupledFieldBuilder;
import se.tink.libraries.i18n_aggregation.Catalog;

@RequiredArgsConstructor
// This version is the old/current way, that we want to still compare to the new one.
public class PostbankLegacyDecoupledFieldBuilder implements DecoupledFieldBuilder {

    private final Catalog catalog;

    @Override
    public List<Field> getInstructionsField(ScaMethodEntity scaMethod) {
        return Collections.singletonList(
                CommonFields.Instruction.build(
                        catalog.getString(
                                PostbankConstants.InfoScreen.INSTRUCTIONS, scaMethod.getName())));
    }
}
