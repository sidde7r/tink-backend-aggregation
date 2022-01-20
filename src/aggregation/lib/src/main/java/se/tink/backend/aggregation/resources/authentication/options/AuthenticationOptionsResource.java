package se.tink.backend.aggregation.resources.authentication.options;

import com.google.inject.Inject;
import java.util.Map;
import java.util.Set;
import se.tink.backend.aggregation.agents.authentication.options.AuthenticationOptionsExtractor;
import se.tink.backend.aggregation.api.AuthenticationOptionsService;
import se.tink.libraries.authentication_options.AuthenticationOptionDto;

public class AuthenticationOptionsResource implements AuthenticationOptionsService {

    private AuthenticationOptionsExtractor authenticationOptionsExtractor;

    @Inject
    public AuthenticationOptionsResource(
            AuthenticationOptionsExtractor authenticationOptionsExtractor) {
        this.authenticationOptionsExtractor = authenticationOptionsExtractor;
    }

    @Override
    public Map<String, Set<AuthenticationOptionDto>> getAgentsAuthenticationOptions() {
        return authenticationOptionsExtractor.getAgentsAuthenticationOptions();
    }
}
