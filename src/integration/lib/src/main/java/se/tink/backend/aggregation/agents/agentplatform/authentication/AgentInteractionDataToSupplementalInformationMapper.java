package se.tink.backend.aggregation.agents.agentplatform.authentication;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.label.ConstantFieldLabel;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

@AllArgsConstructor
public class AgentInteractionDataToSupplementalInformationMapper {

    private final SupplementalInformationFormer supplementalInformationFormer;

    public Field toField(final AgentFieldDefinition fieldDefinition) {
        Field field = supplementalInformationFormer.getField(fieldDefinition.getFieldIdentifier());
        if (fieldDefinition.getFieldLabel() instanceof ConstantFieldLabel) {
            field.setValue(fieldDefinition.getFieldLabel().getLabel());
        }
        return field;
    }

    public Field[] toFields(final List<AgentFieldDefinition> fieldDefinitions) {
        return fieldDefinitions.stream()
                .map(fieldDefinition -> toField(fieldDefinition))
                .collect(Collectors.toList())
                .toArray(new Field[fieldDefinitions.size()]);
    }

    public ThirdPartyRequestWithStateParam toThirdPartyRequest(
            final String redirectURL, AgentClientInfo agentClientInfo) {
        return new ThirdPartyRequestWithStateParam(
                redirectURL, 9, TimeUnit.MINUTES, agentClientInfo);
    }
}
