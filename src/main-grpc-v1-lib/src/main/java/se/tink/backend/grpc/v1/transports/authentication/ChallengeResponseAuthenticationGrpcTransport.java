package se.tink.backend.grpc.v1.transports.authentication;

import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import javax.inject.Inject;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.core.exceptions.AuthenticationKeyNotFoundException;
import se.tink.backend.core.exceptions.AuthenticationTokenExpiredException;
import se.tink.backend.core.exceptions.AuthenticationTokenNotFoundException;
import se.tink.backend.core.exceptions.ChallengeExpiredException;
import se.tink.backend.core.exceptions.ChallengeNotFoundException;
import se.tink.backend.grpc.v1.converter.authentication.SignedChallengeAuthenticationResponseConverter;
import se.tink.backend.grpc.v1.converter.authentication.keys.AuthenticationKeyResponseConverter;
import se.tink.backend.grpc.v1.converter.authentication.keys.StoreAuthenticationKeyRequestConverter;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.grpc.v1.interceptors.RequestHeadersInterceptor;
import se.tink.backend.grpc.v1.utils.TinkGrpcHeaders;
import se.tink.backend.main.auth.DefaultAuthenticationContext;
import se.tink.backend.main.controllers.ChallengeResponseAuthenticationServiceController;
import se.tink.backend.rpc.auth.AuthenticationResponse;
import se.tink.backend.rpc.auth.abnamro.SignedChallengeAuthenticationCommand;
import se.tink.backend.rpc.auth.keys.DeleteAuthenticationKeyCommand;
import se.tink.backend.rpc.auth.keys.StoreAuthenticationKeyCommand;
import se.tink.grpc.v1.rpc.DeleteAuthenticationKeyRequest;
import se.tink.grpc.v1.rpc.DeleteAuthenticationKeyResponse;
import se.tink.grpc.v1.rpc.GetAuthenticationChallengeRequest;
import se.tink.grpc.v1.rpc.GetAuthenticationChallengeResponse;
import se.tink.grpc.v1.rpc.ListAuthenticationKeysRequest;
import se.tink.grpc.v1.rpc.ListAuthenticationKeysResponse;
import se.tink.grpc.v1.rpc.SignedChallengeAuthenticationRequest;
import se.tink.grpc.v1.rpc.SignedChallengeAuthenticationResponse;
import se.tink.grpc.v1.rpc.StoreAuthenticationKeyRequest;
import se.tink.grpc.v1.rpc.StoreAuthenticationKeyResponse;
import se.tink.grpc.v1.services.ChallengeResponseAuthenticationServiceGrpc;

public class ChallengeResponseAuthenticationGrpcTransport
        extends ChallengeResponseAuthenticationServiceGrpc.ChallengeResponseAuthenticationServiceImplBase {
    private final ChallengeResponseAuthenticationServiceController serviceController;

    @Inject
    public ChallengeResponseAuthenticationGrpcTransport(
            ChallengeResponseAuthenticationServiceController serviceController) {
        this.serviceController = serviceController;
    }

    @Override
    @Authenticated
    public void listAuthenticationKeys(ListAuthenticationKeysRequest request,
            StreamObserver<ListAuthenticationKeysResponse> responseObserver) {
        DefaultAuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        String userId = authenticationContext.getUser().getId();
        AuthenticationKeyResponseConverter converter = new AuthenticationKeyResponseConverter();

        ListAuthenticationKeysResponse response = ListAuthenticationKeysResponse.newBuilder()
                .addAllAuthenticationKeys(converter.convertFrom(serviceController.listAuthenticationKeys(userId)))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @Authenticated
    public void storeAuthenticationKey(StoreAuthenticationKeyRequest request,
            StreamObserver<StoreAuthenticationKeyResponse> responseObserver) {
        DefaultAuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        StoreAuthenticationKeyCommand command = new StoreAuthenticationKeyRequestConverter().convertFrom(request);
        authenticationContext.getUserDeviceId().ifPresent(command::setDeviceId);

        StoreAuthenticationKeyResponse.Builder builder = StoreAuthenticationKeyResponse.newBuilder();
        try {
            builder.setKeyId(serviceController.storeAuthenticationKey(command));
        } catch (UnsupportedOperationException e) {
            throw ApiError.NOT_IMPLEMENTED.withCause(e).exception();
        } catch (AuthenticationTokenNotFoundException e) {
            throw ApiError.Authentication.AuthenticationToken.NOT_FOUND.withCause(e).exception();
        } catch (AuthenticationTokenExpiredException e) {
            throw ApiError.Authentication.AuthenticationToken.EXPIRED.withCause(e).exception();
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    @Authenticated
    public void deleteAuthenticationKey(DeleteAuthenticationKeyRequest request,
            StreamObserver<DeleteAuthenticationKeyResponse> responseObserver) {
        DefaultAuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        DeleteAuthenticationKeyCommand command = new DeleteAuthenticationKeyCommand();
        command.setKeyId(request.getKeyId());
        command.setUserId(authenticationContext.getUser().getId());

        serviceController.deleteAuthenticationKey(command);

        responseObserver.onNext(DeleteAuthenticationKeyResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    @Authenticated(required = false)
    public void getAuthenticationChallenge(GetAuthenticationChallengeRequest request,
            StreamObserver<GetAuthenticationChallengeResponse> responseObserver) {
        try {
            GetAuthenticationChallengeResponse response = GetAuthenticationChallengeResponse.newBuilder()
                    .setChallenge(serviceController.createAuthenticationChallenge(request.getKeyId()))
                    .build();
            responseObserver.onNext(response);
        } catch (AuthenticationKeyNotFoundException e) {
            throw ApiError.PublicKeys.NOT_FOUND.withCause(e).exception();
        }

        responseObserver.onCompleted();
    }

    @Override
    @Authenticated(required = false)
    public void signedChallengeAuthentication(SignedChallengeAuthenticationRequest request,
            StreamObserver<SignedChallengeAuthenticationResponse> responseObserver) {
        Metadata headers = RequestHeadersInterceptor.HEADERS.get();
        DefaultAuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        SignedChallengeAuthenticationCommand command = SignedChallengeAuthenticationCommand.builder()
                .withToken(request.getSignedChallenge())
                .withClientKey(headers.get(TinkGrpcHeaders.CLIENT_KEY_HEADER_NAME))
                .withOauthClientId(headers.get(TinkGrpcHeaders.OAUTH_CLIENT_ID_HEADER_NAME))
                .withRemoteAddress(authenticationContext.getRemoteAddress())
                .withUserAgent(authenticationContext.getUserAgent().orElse(null))
                .withUserDeviceId(authenticationContext.getUserDeviceId().orElse(null))
                .withMarket(null) // TODO: Add option to set market in request
                .build();

        try {
            AuthenticationResponse response = serviceController.signedChallengeAuthentication(command);

            responseObserver.onNext(new SignedChallengeAuthenticationResponseConverter().convertFrom(response));
        } catch (ChallengeNotFoundException e) {
            throw ApiError.Authentication.Challenge.NOT_FOUND.withCause(e).exception();
        } catch (ChallengeExpiredException e) {
            throw ApiError.Authentication.Challenge.EXPIRED.withCause(e).exception();
        }
        responseObserver.onCompleted();
    }
}
