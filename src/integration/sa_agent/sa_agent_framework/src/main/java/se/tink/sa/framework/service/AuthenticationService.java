package se.tink.sa.framework.service;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.framework.facade.AuthenticationFacade;
import se.tink.sa.model.auth.AuthenticationRequest;
import se.tink.sa.model.auth.AuthenticationResponse;
import se.tink.sa.services.auth.ProgressiveAuthAgentServiceGrpc;

@Slf4j
@Component
public class AuthenticationService
        extends ProgressiveAuthAgentServiceGrpc.ProgressiveAuthAgentServiceImplBase {

    @Autowired private AuthenticationFacade authenticationFacade;

    @Override
    public void login(
            AuthenticationRequest request,
            StreamObserver<AuthenticationResponse> responseObserver) {
        log.info("Incomming message: {}", request);
        AuthenticationResponse response = authenticationFacade.getConsent(request);
        log.info("Outgoing message {}", response);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
