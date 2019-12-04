package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;

public class SupplementalFieldsAuthenticationStep implements AuthenticationStep {

    public interface CallbackProcessor {
        void process(final Map<String, String> nameValueCallback) throws AuthenticationException;
    }

    private List<Field> fields = new LinkedList<>();
    private CallbackProcessor callbackProcessor;

    public SupplementalFieldsAuthenticationStep(
            final CallbackProcessor callbackProcessor, final Field... fields) {
        Arrays.stream(fields).forEach(f -> this.fields.add(f));
        this.callbackProcessor = callbackProcessor;
    }

    @Override
    public Optional<SupplementInformationRequester> execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        if (request.getUserInputs() == null || request.getUserInputs().isEmpty()) {
            return Optional.of(
                    new SupplementInformationRequester.Builder().withFields(fields).build());
        }
        callbackProcessor.process(request.getUserInputs());
        return Optional.empty();
    }
}
