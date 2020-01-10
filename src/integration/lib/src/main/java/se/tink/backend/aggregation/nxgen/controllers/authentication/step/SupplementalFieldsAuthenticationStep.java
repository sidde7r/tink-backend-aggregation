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

    private List<Field> fields = new LinkedList<>();
    private CallbackProcessorMultiData callbackProcessor;
    private CallbackProcessorMultiDataAndCredentials callbackProcessorCredentials;

    public SupplementalFieldsAuthenticationStep(
            final CallbackProcessorMultiData callbackProcessor, final Field... fields) {
        Arrays.stream(fields).forEach(f -> this.fields.add(f));
        this.callbackProcessor = callbackProcessor;
    }

    public SupplementalFieldsAuthenticationStep(
            final CallbackProcessorMultiDataAndCredentials callbackProcessor,
            final Field... fields) {
        Arrays.stream(fields).forEach(f -> this.fields.add(f));
        this.callbackProcessorCredentials = callbackProcessor;
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
}
