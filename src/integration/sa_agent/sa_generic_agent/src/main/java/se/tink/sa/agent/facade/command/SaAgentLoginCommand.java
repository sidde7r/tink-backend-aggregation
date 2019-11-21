package se.tink.sa.agent.facade.command;

import io.grpc.ManagedChannel;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.sa.model.auth.AuthenticationRequest;
import se.tink.sa.model.auth.AuthenticationResponse;
import se.tink.sa.services.auth.ProgressiveAuthAgentServiceGrpc;

public class SaAgentLoginCommand
        extends AbstractCommand<SteppableAuthenticationRequest, SteppableAuthenticationResponse> {

    private static final Logger log = LoggerFactory.getLogger(SaAgentLoginCommand.class);

    @Override
    protected SteppableAuthenticationResponse doExecute(
            ManagedChannel channel, SteppableAuthenticationRequest request) {
        ProgressiveAuthAgentServiceGrpc.ProgressiveAuthAgentServiceBlockingStub stub =
                ProgressiveAuthAgentServiceGrpc.newBlockingStub(channel);

        AuthenticationRequest.Builder requestBuilder = AuthenticationRequest.newBuilder();
        requestBuilder.setCorrelationId(UUID.randomUUID().toString());

        AuthenticationRequest authenticationRequest = requestBuilder.build();

        AuthenticationResponse response = stub.login(authenticationRequest);

        log.info("Response: {}", response);

        // TODO: implement me
        return SteppableAuthenticationResponse.finalResponse(
                se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse
                        .empty());
    }
}
