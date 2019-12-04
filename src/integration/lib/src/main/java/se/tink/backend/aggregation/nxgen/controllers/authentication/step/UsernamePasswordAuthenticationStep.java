package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;

public class UsernamePasswordAuthenticationStep implements AuthenticationStep {

    public interface CallbackProcessor {
        void process(final String username, final String password) throws AuthenticationException;
    }

    private final CallbackProcessor processor;

    public UsernamePasswordAuthenticationStep(CallbackProcessor processor) {
        this.processor = processor;
    }

    @Override
    public Optional<SupplementInformationRequester> execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        String username = request.getCredentials().getField(Field.Key.USERNAME);
        String password = request.getCredentials().getField(Key.PASSWORD);
        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        processor.process(username, password);
        return Optional.empty();
    }
}
