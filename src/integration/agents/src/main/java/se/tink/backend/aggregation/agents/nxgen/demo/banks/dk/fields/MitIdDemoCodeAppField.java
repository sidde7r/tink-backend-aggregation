package se.tink.backend.aggregation.agents.nxgen.demo.banks.dk.fields;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.DecoupledTemplate;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.dto.DecoupledData;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MitIdDemoCodeAppField {

    public static List<Field> build() {
        return DecoupledTemplate.getTemplate(
                DecoupledData.builder()
                        .iconUrl(
                                "https://www.mitid.dk/mitid-code-app-auth/assets/img/code-app-slider-emulator.gif")
                        .text("To continue, open your MitID app")
                        .build());
    }
}
