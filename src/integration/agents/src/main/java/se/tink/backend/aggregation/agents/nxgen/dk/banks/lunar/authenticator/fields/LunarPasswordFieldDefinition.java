package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.fields;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.label.AgentFieldLabel;

@RequiredArgsConstructor
public class LunarPasswordFieldDefinition implements AgentFieldDefinition {

    private final String fieldIdentifier;

    @Override
    public String getFieldIdentifier() {
        return fieldIdentifier;
    }

    @Override
    public AgentFieldLabel getFieldLabel() {
        return () -> "";
    }
}
