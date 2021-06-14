package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.smscode;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.FieldsBuilder;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.smscode.dto.SmsCodeData;

public class SmsCodeTemplate {

    private static final String TEMPLATE_NAME = "SMS_CODE";

    private static final String ICON = "icon";
    private static final String TITLE = "title";
    private static final String INPUT = "input";
    private static final String INSTRUCTIONS = "instructions";

    public static List<Field> getTemplate(SmsCodeData smsCodeData) {
        List<Field> templatesList = new ArrayList<>();
        templatesList.add(FieldsBuilder.buildTemplateField(TEMPLATE_NAME));
        templatesList.add(FieldsBuilder.buildIconField(smsCodeData.getIconUrl(), ICON));
        templatesList.add(FieldsBuilder.buildTitleField(smsCodeData.getTitle(), TITLE));
        templatesList.add(FieldsBuilder.buildInputField(smsCodeData.getInput(), INPUT));
        templatesList.add(
                FieldsBuilder.buildInstructionsListField(
                        smsCodeData.getInstructions(), INSTRUCTIONS));
        return templatesList;
    }
}
