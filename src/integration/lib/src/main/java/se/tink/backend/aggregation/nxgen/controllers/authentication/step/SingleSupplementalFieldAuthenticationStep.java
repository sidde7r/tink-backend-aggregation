package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import se.tink.backend.agents.rpc.Field;

public class SingleSupplementalFieldAuthenticationStep
        extends SupplementalFieldsAuthenticationStep {

    public SingleSupplementalFieldAuthenticationStep(
            SingleFieldCallbackProcessor callbackProcessor, Field field) {
        super((fields) -> callbackProcessor.process(fields.get(field.getName())), field);
    }
}
