package se.tink.sa.framework.command.auth;

import lombok.extern.slf4j.Slf4j;
import se.tink.sa.framework.command.AbstractCommand;
import se.tink.sa.model.auth.AuthenticationRequest;
import se.tink.sa.model.auth.AuthenticationResponse;

@Slf4j
public class AuthorizationBuildUrlCommand
        extends AbstractCommand<AuthenticationRequest, AuthenticationResponse> {

    @Override
    protected AuthenticationResponse doExecute(AuthenticationRequest request) {
        log.info("Executing command");
        AuthenticationResponse.Builder builder = AuthenticationResponse.newBuilder();
        builder.setCorrelationId(request.getCorrelationId());
        AuthenticationResponse response = builder.build();
        log.info("Executing command ends");
        return response;
    }
}
