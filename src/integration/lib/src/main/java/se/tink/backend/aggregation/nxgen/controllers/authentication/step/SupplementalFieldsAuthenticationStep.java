package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;

public class SupplementalFieldsAuthenticationStep extends AbstractAuthenticationStep {

    protected final List<Field> fields = new LinkedList<>();
    private CallbackProcessorMultiData callbackProcessor;
    private CallbackProcessorMultiDataAndCredentials callbackProcessorCredentials;

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
        super(stepId);
        initFields(fields);
    }

    private void initFields(Field... fields) {
        Arrays.stream(fields).forEach(f -> this.fields.add(f));
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        if (request.getUserInputs() == null || request.getUserInputs().isEmpty()) {
            return AuthenticationStepResponse.requestForSupplementInformation(
                    new SupplementInformationRequester.Builder().withFields(fields).build());
        }
        callback(request);
        return AuthenticationStepResponse.executeNextStep();
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
