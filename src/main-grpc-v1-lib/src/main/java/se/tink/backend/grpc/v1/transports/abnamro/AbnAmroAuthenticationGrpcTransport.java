package se.tink.backend.grpc.v1.transports.abnamro;

import com.google.inject.Inject;
import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.grpc.v1.converter.abnamro.AbnAmroAuthenticationResponseConverter;
import se.tink.backend.grpc.v1.interceptors.RequestHeadersInterceptor;
import se.tink.backend.grpc.v1.utils.TinkGrpcHeaders;
import se.tink.backend.main.controllers.abnamro.AbnAmroAuthenticationController;
import se.tink.backend.rpc.auth.AuthenticationResponse;
import se.tink.backend.rpc.auth.abnamro.AbnAmroAuthenticationCommand;
import se.tink.grpc.v1.rpc.AbnAmroAuthenticationRequest;
import se.tink.grpc.v1.rpc.AbnAmroAuthenticationResponse;
import se.tink.grpc.v1.services.AbnAmroAuthenticationServiceGrpc;

public class AbnAmroAuthenticationGrpcTransport
        extends AbnAmroAuthenticationServiceGrpc.AbnAmroAuthenticationServiceImplBase {
    private AbnAmroAuthenticationController authenticationController;

    @Inject
    public AbnAmroAuthenticationGrpcTransport(AbnAmroAuthenticationController authenticationController) {
        this.authenticationController = authenticationController;
    }

    @Override
    @Authenticated(required = false, requireAuthorizedDevice = false)
    public void authenticate(AbnAmroAuthenticationRequest request,
            StreamObserver<AbnAmroAuthenticationResponse> observer) {
        Metadata headers = RequestHeadersInterceptor.HEADERS.get();

        AbnAmroAuthenticationCommand command = AbnAmroAuthenticationCommand.builder()
                .withInternetBankingSessionToken(request.getInternetBankingSessionToken())
                .withClientKey(headers.get(TinkGrpcHeaders.CLIENT_KEY_HEADER_NAME))
                .withOauthClientId(headers.get(TinkGrpcHeaders.OAUTH_CLIENT_ID_HEADER_NAME))
                .build();

        AuthenticationResponse response = authenticationController.authenticate(command);
        observer.onNext(new AbnAmroAuthenticationResponseConverter().convertFrom(response));
        observer.onCompleted();
    }
}
