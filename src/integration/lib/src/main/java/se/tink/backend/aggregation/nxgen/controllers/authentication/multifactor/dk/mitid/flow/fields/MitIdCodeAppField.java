package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.fields;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.DecoupledTemplate;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.dto.DecoupledData;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MitIdCodeAppField {

    public static final String BUTTON_FIELD_NAME = "mitIdCodeAppContinueButtonField";

    private static final String ICON_URL =
            "https://www.mitid.dk/mitid-code-app-auth/assets/img/code-app-slider-emulator.gif";
    private static final LocalizableKey SCREEN_TITLE =
            new LocalizableKey("To continue, open your MitID app");
    private static final LocalizableKey CONTINUE_BUTTON_TEXT = new LocalizableKey("Continue");

    public static List<Field> build(Catalog catalog) {
        List<Field> fields =
                DecoupledTemplate.getTemplate(
                        DecoupledData.builder()
                                .iconUrl(ICON_URL)
                                .text(catalog.getString(SCREEN_TITLE))
                                .build());
        fields.add(
                Field.builder()
                        .name(BUTTON_FIELD_NAME)
                        .description("")
                        .type("OPEN_THIRD_PARTY")
                        .value(catalog.getString(CONTINUE_BUTTON_TEXT))
                        .build());
        return fields;
    }
}
