package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication;

import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.CallbackProcessorEmpty;

public class ModuleVersionAuthenticationStep extends AutomaticAuthenticationStep {

    public ModuleVersionAuthenticationStep(CallbackProcessorEmpty processCallback) {
        super(processCallback, ModuleVersionAuthenticationStep.class.getName());
    }
}
