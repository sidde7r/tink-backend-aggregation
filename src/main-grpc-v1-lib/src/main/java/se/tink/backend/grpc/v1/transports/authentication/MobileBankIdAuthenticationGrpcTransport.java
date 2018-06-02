package se.tink.backend.grpc.v1.transports.authentication;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.TimeoutException;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.core.exceptions.AuthenticationTokenExpiredException;
import se.tink.backend.core.exceptions.AuthenticationTokenNotFoundException;
import se.tink.backend.core.exceptions.AuthenticationTokenNotValidException;
import se.tink.backend.core.exceptions.BankIdAuthenticationExpiredException;
import se.tink.backend.core.exceptions.BankIdAuthenticationNotFoundException;
import se.tink.backend.grpc.v1.converter.authentication.CoreCollectBankIdAuthenticationResponseToGrpcConverter;
import se.tink.backend.grpc.v1.converter.authentication.CoreInitiateBankIdAuthenticationResponseToGrpcConverter;
import se.tink.backend.grpc.v1.converter.authentication.InitiateBankIdAuthenticationRequestConverter;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.AuthenticationServiceController;
import se.tink.backend.main.controllers.exceptions.UserNotFoundException;
import se.tink.backend.rpc.InitiateBankIdAuthenticationCommand;
import se.tink.grpc.v1.rpc.CollectBankIdAuthenticationRequest;
import se.tink.grpc.v1.rpc.CollectBankIdAuthenticationResponse;
import se.tink.grpc.v1.rpc.InitiateBankIdAuthenticationRequest;
import se.tink.grpc.v1.rpc.InitiateBankIdAuthenticationResponse;
import se.tink.grpc.v1.services.MobileBankIdAuthenticationServiceGrpc;

public class MobileBankIdAuthenticationGrpcTransport
        extends MobileBankIdAuthenticationServiceGrpc.MobileBankIdAuthenticationServiceImplBase {

    private AuthenticationServiceController authenticationServiceController;

    @Inject
    public MobileBankIdAuthenticationGrpcTransport(AuthenticationServiceController authenticationServiceController) {
        this.authenticationServiceController = authenticationServiceController;
    }

    @Override
    @Authenticated(required = false, requireAuthorizedDevice = false)
    public void initiateBankIdAuthentication(InitiateBankIdAuthenticationRequest initiateBankIdAuthenticationRequest,
            StreamObserver<InitiateBankIdAuthenticationResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        InitiateBankIdAuthenticationCommand command = new InitiateBankIdAuthenticationRequestConverter(
                authenticationContext).convertFrom(initiateBankIdAuthenticationRequest);

        try {
            InitiateBankIdAuthenticationResponse response = new CoreInitiateBankIdAuthenticationResponseToGrpcConverter()
                    .convertFrom(authenticationServiceController.initiateBankIdAuthentication(command));
            streamObserver.onNext(response);
            streamObserver.onCompleted();
        } catch (TimeoutException e) {
            throw ApiError.Authentication.MobileBankId.TIMED_OUT.withCause(e).exception();
        } catch (InterruptedException e) {
            throw ApiError.Authentication.MobileBankId.ABORTED.withCause(e).exception();
        } catch (UserNotFoundException e) {
            throw ApiError.Users.NOT_FOUND.withCause(e).exception();
        } catch (AuthenticationTokenExpiredException e) {
            throw ApiError.Authentication.AuthenticationToken.EXPIRED.withCause(e).exception();
        } catch (AuthenticationTokenNotValidException e) {
            throw ApiError.Authentication.AuthenticationToken.INVALID.withCause(e).exception();
        } catch (AuthenticationTokenNotFoundException e) {
            throw ApiError.Authentication.AuthenticationToken.NOT_FOUND.withCause(e).exception();
        }
    }

    @Override
    @Authenticated(required = false, requireAuthorizedDevice = false)
    public void collectBankIdAuthentication(CollectBankIdAuthenticationRequest collectBankIdAuthenticationRequest,
            StreamObserver<CollectBankIdAuthenticationResponse> streamObserver) {
        try {
            CollectBankIdAuthenticationResponse response = new CoreCollectBankIdAuthenticationResponseToGrpcConverter()
                    .convertFrom(authenticationServiceController
                            .collectBankIdAuthentication(collectBankIdAuthenticationRequest.getAuthenticationToken()));
            streamObserver.onNext(response);
            streamObserver.onCompleted();
        } catch (BankIdAuthenticationNotFoundException e) {
            throw ApiError.Authentication.MobileBankId.NOT_FOUND.withCause(e).withInfoSeverity().exception();
        } catch (BankIdAuthenticationExpiredException e) {
            throw ApiError.Authentication.MobileBankId.EXPIRED.withCause(e).withInfoSeverity().exception();
        }
    }
}
