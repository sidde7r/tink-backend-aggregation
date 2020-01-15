package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;

public class SupplementalFieldsAuthenticationStep implements AuthenticationStep {

    private final List<Field> fields = new LinkedList<>();
    private CallbackProcessorMultiData callbackProcessor;
    private CallbackProcessorMultiDataAndCredentials callbackProcessorCredentials;
    private final String stepId;

    public SupplementalFieldsAuthenticationStep(
            final String stepId,
            final CallbackProcessorMultiData callbackProcessor,
            final Field... fields) {
        this(stepId, fields);
        this.callbackProcessor = callbackProcessor;
    }

    public SupplementalFieldsAuthenticationStep(
            final String stepId,
            final CallbackProcessorMultiDataAndCredentials callbackProcessor,
            final Field... fields) {
        this(stepId, fields);
        this.callbackProcessorCredentials = callbackProcessor;
    }

    private SupplementalFieldsAuthenticationStep(final String stepId, final Field... fields) {
        Arrays.stream(fields).forEach(f -> this.fields.add(f));
        this.stepId = stepId;
    }

    @Override
    public Optional<SupplementInformationRequester> execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        if (request.getUserInputs() == null || request.getUserInputs().isEmpty()) {
            return Optional.of(
                    new SupplementInformationRequester.Builder().withFields(fields).build());
        }
        callback(request);
        return Optional.empty();
    }

    private void callback(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        if (callbackProcessor != null) {
            callbackProcessor.process(request.getUserInputs());
        } else {
            callbackProcessorCredentials.process(request.getUserInputs(), request.getCredentials());
        }
    }

    @Override
    public String getIdentifier() {
        return stepId;
    }
}
