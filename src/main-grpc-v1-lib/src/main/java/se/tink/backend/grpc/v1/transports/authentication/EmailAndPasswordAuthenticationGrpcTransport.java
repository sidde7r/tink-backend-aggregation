package se.tink.backend.grpc.v1.transports.authentication;

import com.google.inject.Inject;
import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import java.util.NoSuchElementException;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.common.mail.MailTemplate;
import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.converter.authentication.CoreAuthenticationResponseToGrpcConverter;
import se.tink.backend.grpc.v1.converter.user.CoreUserToGrpcUserProfileConverter;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.grpc.v1.interceptors.RequestHeadersInterceptor;
import se.tink.backend.grpc.v1.utils.TinkGrpcHeaders;
import se.tink.backend.main.auth.DefaultAuthenticationContext;
import se.tink.backend.main.controllers.AuthenticationServiceController;
import se.tink.backend.main.controllers.EmailAndPasswordAuthenticationServiceController;
import se.tink.backend.rpc.EmailAndPasswordAuthenticationCommand;
import se.tink.backend.rpc.ForgotPasswordCommand;
import se.tink.backend.rpc.ResetPasswordCommand;
import se.tink.backend.rpc.UpdateEmailCommand;
import se.tink.backend.rpc.UpdatePasswordCommand;
import se.tink.grpc.v1.rpc.EmailAndPasswordAuthenticationRequest;
import se.tink.grpc.v1.rpc.EmailAndPasswordAuthenticationResponse;
import se.tink.grpc.v1.rpc.ForgotPasswordRequest;
import se.tink.grpc.v1.rpc.ForgotPasswordResponse;
import se.tink.grpc.v1.rpc.ResetPasswordRequest;
import se.tink.grpc.v1.rpc.ResetPasswordResponse;
import se.tink.grpc.v1.rpc.UpdateEmailRequest;
import se.tink.grpc.v1.rpc.UpdateEmailResponse;
import se.tink.grpc.v1.rpc.UpdatePasswordRequest;
import se.tink.grpc.v1.rpc.UpdatePasswordResponse;
import se.tink.grpc.v1.services.EmailAndPasswordAuthenticationServiceGrpc;
import se.tink.libraries.validation.exceptions.InvalidEmailException;

public class EmailAndPasswordAuthenticationGrpcTransport
        extends EmailAndPasswordAuthenticationServiceGrpc.EmailAndPasswordAuthenticationServiceImplBase {
    private final EmailAndPasswordAuthenticationServiceController emailAndPasswordAuthenticationServiceController;
    private final AuthenticationServiceController authenticationServiceController;

    @Inject
    public EmailAndPasswordAuthenticationGrpcTransport(
            EmailAndPasswordAuthenticationServiceController emailAndPasswordAuthenticationServiceController,
            AuthenticationServiceController authenticationServiceController) {
        this.emailAndPasswordAuthenticationServiceController = emailAndPasswordAuthenticationServiceController;
        this.authenticationServiceController = authenticationServiceController;
    }

    @Override
    @Authenticated(required = false, requireAuthorizedDevice = false)
    public void emailAndPasswordAuthentication(EmailAndPasswordAuthenticationRequest basicAuthenticationRequest,
            StreamObserver<EmailAndPasswordAuthenticationResponse> streamObserver) {

        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            Metadata headers = RequestHeadersInterceptor.HEADERS.get();
            EmailAndPasswordAuthenticationCommand command = new EmailAndPasswordAuthenticationCommand(
                    basicAuthenticationRequest.getEmail(),
                    basicAuthenticationRequest.getPassword(),
                    basicAuthenticationRequest.getMarketCode(),
                    headers.get(TinkGrpcHeaders.CLIENT_KEY_HEADER_NAME),
                    headers.get(TinkGrpcHeaders.OAUTH_CLIENT_ID_HEADER_NAME),
                    authenticationContext.getUserDeviceId().orElse(null),
                    authenticationContext.getUserAgent().orElse(null));
            EmailAndPasswordAuthenticationResponse response = new CoreAuthenticationResponseToGrpcConverter()
                    .convertFrom(authenticationServiceController.authenticateEmailAndPassword(command));
            streamObserver.onNext(response);
            streamObserver.onCompleted();
        } catch (InvalidEmailException e) {
            throw ApiError.Validation.INVALID_EMAIL.withWarnSeverity().withCause(e).exception();
        }
    }

    @Override
    @Authenticated(required = false)
    public void forgotPassword(ForgotPasswordRequest request, StreamObserver<ForgotPasswordResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            emailAndPasswordAuthenticationServiceController.forgotPassword(new ForgotPasswordCommand(request.getEmail(),
                    authenticationContext.getRemoteAddress(),
                    authenticationContext.getUserAgent()), MailTemplate.FORGOT_PASSWORD);
            responseObserver.onNext(ForgotPasswordResponse.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (InvalidEmailException e) {
            throw ApiError.Validation.INVALID_EMAIL.withWarnSeverity().withCause(e).exception();
        } catch (NoSuchElementException e) {
            throw ApiError.Users.NOT_FOUND.withCause(e).exception();
        }
    }

    @Override
    @Authenticated(required = false)
    public void resetPassword(ResetPasswordRequest request, StreamObserver<ResetPasswordResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        emailAndPasswordAuthenticationServiceController.resetPassword(new ResetPasswordCommand(request.getTokenId(),
                request.getPassword(),
                authenticationContext.getRemoteAddress()));
        responseObserver.onNext(ResetPasswordResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    @Authenticated
    public void updatePassword(UpdatePasswordRequest request, StreamObserver<UpdatePasswordResponse> responseObserver) {
        DefaultAuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        emailAndPasswordAuthenticationServiceController.updatePassword(authenticationContext.getUser(),
                new UpdatePasswordCommand(request.getOldPassword(),
                        request.getNewPassword(),
                        authenticationContext.getAuthenticationDetails().getSessionId().orElse(null),
                        authenticationContext.getRemoteAddress()));
        responseObserver.onNext(UpdatePasswordResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    @Authenticated
    public void updateEmail(UpdateEmailRequest updateEmailRequest,
            StreamObserver<UpdateEmailResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            UpdateEmailCommand command = new UpdateEmailCommand(updateEmailRequest.getEmail().getValue());

            User updatedUser = emailAndPasswordAuthenticationServiceController
                    .updateEmail(authenticationContext.getUser(), command,
                            authenticationContext.getRemoteAddress());
            streamObserver.onNext(UpdateEmailResponse.newBuilder()
                    .setUserProfile(new CoreUserToGrpcUserProfileConverter().convertFrom(updatedUser))
                    .build());
            streamObserver.onCompleted();
        } catch (InvalidEmailException e) {
            throw ApiError.Validation.INVALID_EMAIL.withWarnSeverity().withCause(e).exception();
        }
    }
}
