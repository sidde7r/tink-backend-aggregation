package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.fields;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.label.AgentFieldLabel;

@RequiredArgsConstructor
public class AgentField implements AgentFieldDefinition {
    private final String fieldIdentifier;
    private final String fieldLabel;

    @Override
    public String getFieldIdentifier() {
        return fieldIdentifier;
    }

    @Override
    public AgentFieldLabel getFieldLabel() {
        return () -> fieldLabel;
    }

    public static AgentField clientnumber() {
        return new AgentField("clientnumber", "field.clientnumber");
    }
}
