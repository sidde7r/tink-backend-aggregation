package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import se.tink.backend.agents.rpc.Field;

public class SingleSupplementalFieldAuthenticationStep
        extends SupplementalFieldsAuthenticationStep {

    public SingleSupplementalFieldAuthenticationStep(
            CallbackProcessorSingleData callbackProcessor, Field field) {
        super((fields) -> callbackProcessor.process(fields.get(field.getName())), field);
    }

    public SingleSupplementalFieldAuthenticationStep(
            CallbackProcessorSingleDataAndCredentials callbackProcessor, Field field) {
        super(
                (fields, credentials) ->
                        callbackProcessor.process(fields.get(field.getName()), credentials),
                field);
    }
}
