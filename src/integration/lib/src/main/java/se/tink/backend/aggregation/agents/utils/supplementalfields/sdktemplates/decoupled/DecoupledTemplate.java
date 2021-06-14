package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.FieldsBuilder;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.dto.DecoupledData;

public class DecoupledTemplate {

    private static final String TEMPLATE_NAME = "DECOUPLED";

    private static final String ICON = "icon";
    private static final String INSTRUCTION = "instruction";

    public static List<Field> getTemplate(DecoupledData decoupledData) {
        List<Field> templatesList = new ArrayList<>();
        templatesList.add(FieldsBuilder.buildTemplateField(TEMPLATE_NAME));
        templatesList.add(FieldsBuilder.buildIconField(decoupledData.getIconUrl(), ICON));
        templatesList.add(
                FieldsBuilder.buildInstructionField(
                        null, decoupledData.getInstruction(), INSTRUCTION));
        return templatesList;
    }
}
