package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication;

import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;

public class ModuleVersionAuthenticationStep extends AutomaticAuthenticationStep {

    public ModuleVersionAuthenticationStep(ProcessCallback processCallback) {
        super(processCallback, ModuleVersionAuthenticationStep.class.getName());
    }
}
