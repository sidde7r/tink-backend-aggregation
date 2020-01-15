package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import se.tink.backend.agents.rpc.Field;

public class SingleSupplementalFieldAuthenticationStep
        extends SupplementalFieldsAuthenticationStep {

    public SingleSupplementalFieldAuthenticationStep(
            final String stepId,
            final CallbackProcessorSingleData callbackProcessor,
            final Field field) {
        super(stepId, (fields) -> callbackProcessor.process(fields.get(field.getName())), field);
    }

    public SingleSupplementalFieldAuthenticationStep(
            final String stepId,
            final CallbackProcessorSingleDataAndCredentials callbackProcessor,
            final Field field) {
        super(
                stepId,
                (fields, credentials) ->
                        callbackProcessor.process(fields.get(field.getName()), credentials),
                field);
    }
}
