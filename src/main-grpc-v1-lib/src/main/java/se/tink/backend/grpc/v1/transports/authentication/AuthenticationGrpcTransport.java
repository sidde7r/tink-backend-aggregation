package se.tink.backend.grpc.v1.transports.authentication;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import java.security.AccessControlException;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.common.exceptions.DuplicateException;
import se.tink.backend.core.exceptions.AuthenticationTokenExpiredException;
import se.tink.backend.core.exceptions.AuthenticationTokenNotFoundException;
import se.tink.backend.core.exceptions.GrowthUserNotAllowedException;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.auth.exceptions.UnauthorizedDeviceException;
import se.tink.backend.main.auth.exceptions.UnsupportedClientException;
import se.tink.backend.main.controllers.AuthenticationServiceController;
import se.tink.backend.rpc.AuthenticatedLoginResponse;
import se.tink.backend.rpc.RegisterAccountCommand;
import se.tink.backend.rpc.UserLogoutCommand;
import se.tink.grpc.v1.rpc.LoginRequest;
import se.tink.grpc.v1.rpc.LoginResponse;
import se.tink.grpc.v1.rpc.LogoutRequest;
import se.tink.grpc.v1.rpc.LogoutResponse;
import se.tink.grpc.v1.rpc.RegisterRequest;
import se.tink.grpc.v1.rpc.RegisterResponse;
import se.tink.grpc.v1.services.AuthenticationServiceGrpc;
import se.tink.libraries.validation.exceptions.InvalidEmailException;
import se.tink.libraries.validation.exceptions.InvalidLocaleException;

public class AuthenticationGrpcTransport extends AuthenticationServiceGrpc.AuthenticationServiceImplBase {
    private AuthenticationServiceController authenticationServiceController;

    @Inject
    public AuthenticationGrpcTransport(AuthenticationServiceController authenticationServiceController) {
        this.authenticationServiceController = authenticationServiceController;
    }

    @Override
    @Authenticated(required = false, requireAuthorizedDevice = false)
    public void login(LoginRequest loginRequest, StreamObserver<LoginResponse> streamObserver) {
        try {
            AuthenticatedLoginResponse authenticatedLoginResponse = authenticationServiceController
                    .authenticatedLogin(loginRequest.getAuthenticationToken(), AuthenticationInterceptor.CONTEXT.get());

            streamObserver.onNext(LoginResponse.newBuilder()
                    .setSessionId(authenticatedLoginResponse.getSessionId()).build());
            streamObserver.onCompleted();
        } catch (AuthenticationTokenNotFoundException e) {
            throw ApiError.Authentication.AuthenticationToken.NOT_FOUND.withCause(e).exception();
        } catch (AuthenticationTokenExpiredException e) {
            throw ApiError.Authentication.AuthenticationToken.EXPIRED.withCause(e).exception();
        } catch (AccessControlException e) {
            throw ApiError.Authentication.AuthenticationToken.INVALID.withCause(e).exception();
        } catch (GrowthUserNotAllowedException e) {
            throw ApiError.Authentication.AuthenticationToken.GROWTH_USER_NOT_ALLOWED.withCause(e).exception();
        } catch (UnauthorizedDeviceException e) {
            throw ApiError.Authentication.UNAUTHORIZED_DEVICE.withCause(e).exception();
        } catch (UnsupportedClientException e) {
            throw ApiError.Authentication.DEPRECATED_CLIENT.withCause(e).exception();
        }
    }

    @Override
    @Authenticated(required = false, requireAuthorizedDevice = false)
    public void register(RegisterRequest registerRequest, StreamObserver<RegisterResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            RegisterAccountCommand command = new RegisterAccountCommand(registerRequest.getAuthenticationToken(),
                    registerRequest.getEmail(), registerRequest.getLocale());

            String sessionId = authenticationServiceController.authenticatedRegister(authenticationContext, command);
            streamObserver.onNext(
                    RegisterResponse.newBuilder().setSessionId(sessionId).build());
            streamObserver.onCompleted();
        } catch (AuthenticationTokenExpiredException e) {
            throw ApiError.Authentication.AuthenticationToken.EXPIRED.withCause(e).exception();
        } catch (DuplicateException e) {
            throw ApiError.Authentication.USER_ALREADY_REGISTERED.withWarnSeverity().withCause(e).exception();
        } catch (AuthenticationTokenNotFoundException e) {
            throw ApiError.Authentication.AuthenticationToken.NOT_FOUND.withCause(e).exception();
        } catch (InvalidEmailException e) {
            throw ApiError.Validation.INVALID_EMAIL.withWarnSeverity().withCause(e).exception();
        } catch (InvalidLocaleException e) {
            throw ApiError.Validation.INVALID_LOCALE.withCause(e).exception();
        } catch (UnauthorizedDeviceException e) {
            throw ApiError.Authentication.UNAUTHORIZED_DEVICE.withCause(e).exception();
        } catch (UnsupportedClientException e) {
            throw ApiError.Authentication.DEPRECATED_CLIENT.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void logout(LogoutRequest request, StreamObserver<LogoutResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        UserLogoutCommand command = new UserLogoutCommand(request.getAutologout(), authenticationContext.getMetadata());
        authenticationServiceController.logout(authenticationContext.getUser(), command);
        responseObserver.onNext(LogoutResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
