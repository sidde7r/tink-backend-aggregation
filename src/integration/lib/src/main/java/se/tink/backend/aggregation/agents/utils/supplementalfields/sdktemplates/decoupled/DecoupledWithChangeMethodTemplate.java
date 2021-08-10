package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled;

import java.util.List;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.FieldsBuilder;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.dto.DecoupledWithChangeMethodData;

public class DecoupledWithChangeMethodTemplate extends DecoupledTemplate {

    public static List<Field> getTemplate(DecoupledWithChangeMethodData decoupledData) {
        List<Field> templatesList = DecoupledTemplate.getTemplate(decoupledData);
        templatesList.add(FieldsBuilder.buildChangeMethodField(decoupledData.getButtonText()));
        return templatesList;
    }
}
