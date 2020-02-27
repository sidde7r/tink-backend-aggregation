package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

public class SupplementalFieldsAuthenticationStep extends AbstractAuthenticationStep {

    protected final List<Field> fields = new LinkedList<>();
    private CallbackProcessorMultiData callbackProcessor;

    public SupplementalFieldsAuthenticationStep(
            final String stepId,
            final CallbackProcessorMultiData callbackProcessor,
            final Field... fields) {
        this(stepId, fields);
        this.callbackProcessor = callbackProcessor;
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
        return callbackProcessor.process(request.getUserInputs());
    }
}
