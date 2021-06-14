package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.idcompletion;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.FieldsBuilder;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.idcompletion.dto.IdCompletionData;

public class IdCompletionTemplate {

    private static final String TEMPLATE_NAME = "ID_COMPLETION";

    private static final String COLOR = "color";
    private static final String TITLE = "title";
    private static final String IDENTITY_HINT = "identityHint";
    private static final String PASSWORD_INPUT = "password";
    private static final String IDENTIFICATIONS = "identifications";

    public static List<Field> getTemplate(IdCompletionData idCompletionData) {
        List<Field> templatesList = new ArrayList<>();
        templatesList.add(FieldsBuilder.buildTemplateField(TEMPLATE_NAME));
        templatesList.add(FieldsBuilder.buildColorField(idCompletionData.getColorHex(), COLOR));
        templatesList.add(FieldsBuilder.buildTitleField(idCompletionData.getTitle(), TITLE));
        templatesList.add(
                FieldsBuilder.buildIdentityHint(
                        idCompletionData.getIdentityHintImage(),
                        idCompletionData.getIdentityHintText(),
                        IDENTITY_HINT));
        templatesList.add(
                FieldsBuilder.buildInputField(idCompletionData.getPasswordInput(), PASSWORD_INPUT));
        templatesList.addAll(
                FieldsBuilder.buildChooseInputFields(
                        idCompletionData.getIdentifications(), IDENTIFICATIONS));
        return templatesList;
    }
}
