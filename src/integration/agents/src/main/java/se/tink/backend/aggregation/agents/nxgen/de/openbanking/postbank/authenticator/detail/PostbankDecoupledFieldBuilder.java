package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.detail;

import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.PostbankConstants;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.de.DecoupledFieldBuilder;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.DecoupledTemplate;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.dto.DecoupledData;
import se.tink.libraries.i18n_aggregation.Catalog;

@RequiredArgsConstructor
// This version is the future standard version, using SDK Templates.
public class PostbankDecoupledFieldBuilder implements DecoupledFieldBuilder {

    private final Catalog catalog;

    @Override
    public List<Field> getInstructionsField(ScaMethodEntity scaMethod) {
        return DecoupledTemplate.getTemplate(
                DecoupledData.builder()
                        .iconUrl(
                                "https://cdn.tink.se/provider-images/de/otp-icons/icon-authenticationType-bestSign.png")
                        .text(
                                catalog.getString(
                                        PostbankConstants.InfoScreen.INSTRUCTIONS,
                                        scaMethod.getName()))
                        .build());
    }
}
