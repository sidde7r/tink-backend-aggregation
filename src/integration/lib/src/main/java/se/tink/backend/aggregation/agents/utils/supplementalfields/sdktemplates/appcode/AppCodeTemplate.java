package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.appcode;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.appcode.dto.AppCodeData;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.FieldsBuilder;

@UtilityClass
public class AppCodeTemplate {

    private static final String TEMPLATE_NAME = "APP_CODE";
    private static final String ICON = "icon";
    private static final String TITLE = "title";
    private static final String INPUT = "input";
    private static final String INSTRUCTIONS = "instructions";

    public static List<Field> getTemplate(AppCodeData appCode) {
        List<Field> templatesList = new ArrayList<>(5);
        templatesList.add(FieldsBuilder.buildTemplateField(TEMPLATE_NAME));
        templatesList.add(FieldsBuilder.buildIconField(appCode.getIconUrl(), ICON));
        templatesList.add(FieldsBuilder.buildTitleField(appCode.getTitle(), TITLE));
        templatesList.add(FieldsBuilder.buildInputField(appCode.getInput(), INPUT));
        templatesList.add(
                FieldsBuilder.buildInstructionsListField(appCode.getInstructions(), INSTRUCTIONS));

        return templatesList;
    }
}
