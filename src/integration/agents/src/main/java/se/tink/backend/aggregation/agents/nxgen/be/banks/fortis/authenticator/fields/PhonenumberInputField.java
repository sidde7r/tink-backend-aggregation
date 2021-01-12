package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.fields;

import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.label.AgentFieldLabel;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.label.I18NFieldLabel;

public class PhonenumberInputField implements AgentFieldDefinition {

    public static final String ID = "mobilenumber";

    @Override
    public String getFieldIdentifier() {
        return ID;
    }

    @Override
    public AgentFieldLabel getFieldLabel() {
        return new I18NFieldLabel("field.mobilenumber");
    }
}
