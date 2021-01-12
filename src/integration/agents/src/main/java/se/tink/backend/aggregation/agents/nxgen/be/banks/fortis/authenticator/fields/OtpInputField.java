package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.fields;

import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.label.AgentFieldLabel;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.label.I18NFieldLabel;

public class OtpInputField implements AgentFieldDefinition {

    public static final String ID = "otpinput";

    @Override
    public String getFieldIdentifier() {
        return ID;
    }

    @Override
    public AgentFieldLabel getFieldLabel() {
        return new I18NFieldLabel("field.otpinput");
    }
}
