package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.smscode;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.TemplatesEnum;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.FieldsBuilder;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.smscode.dto.SmsCodeData;

public class SmsCodeTemplate {

    private static final String ICON = "icon";
    private static final String TITLE = "title";
    private static final String INPUT = "input";

    public static List<Field> getTemplate(SmsCodeData smsCodeData) {
        List<Field> templatesList = new ArrayList<>();
        templatesList.add(FieldsBuilder.buildTemplateField(TemplatesEnum.SMS_CODE));
        templatesList.add(FieldsBuilder.buildIconField(smsCodeData.getIconUrl(), ICON));
        templatesList.add(FieldsBuilder.buildTitleField(smsCodeData.getTitle(), TITLE));
        templatesList.add(FieldsBuilder.buildInputField(smsCodeData.getInput(), INPUT));
        return templatesList;
    }
}
