package se.tink.backend.grpc.v1.transports;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.NoSuchElementException;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.core.User;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.grpc.v1.converter.account.CoreAccountToGrpcAccountConverter;
import se.tink.backend.grpc.v1.converter.account.UpdateAccountRequestConverter;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.AccountServiceController;
import se.tink.backend.main.controllers.CredentialServiceController;
import se.tink.grpc.v1.models.Account;
import se.tink.grpc.v1.rpc.ListAccountsRequest;
import se.tink.grpc.v1.rpc.ListAccountsResponse;
import se.tink.grpc.v1.rpc.UpdateAccountRequest;
import se.tink.grpc.v1.rpc.UpdateAccountResponse;
import se.tink.grpc.v1.services.AccountServiceGrpc;

public class AccountGrpcTransport extends AccountServiceGrpc.AccountServiceImplBase {
    private final AccountServiceController accountServiceController;
    private final CredentialServiceController credentialServiceController;

    @Inject
    AccountGrpcTransport(AccountServiceController accountServiceController,
            CredentialServiceController credentialServiceController) {
        this.accountServiceController = accountServiceController;
        this.credentialServiceController = credentialServiceController;
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.ACCOUNTS_WRITE)
    public void updateAccount(
            UpdateAccountRequest request, StreamObserver<UpdateAccountResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        se.tink.backend.rpc.UpdateAccountRequest updateAccountRequest = new UpdateAccountRequestConverter()
                .convertFrom(request);

        try {
            Account updatedAccount = new CoreAccountToGrpcAccountConverter(user.getProfile().getCurrency(),
                    credentialServiceController.getProvidersByCredentialIds(user.getId()))
                    .convertFrom(accountServiceController
                            .update(user.getId(), request.getAccountId(), updateAccountRequest));

            responseObserver.onNext(UpdateAccountResponse.newBuilder().setAccount(updatedAccount).build());
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            throw ApiError.Accounts.NOT_FOUND.withCause(e).exception();
        }
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.ACCOUNTS_READ)
    public void listAccounts(
            ListAccountsRequest request, StreamObserver<ListAccountsResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        List<Account> accounts = new CoreAccountToGrpcAccountConverter(user.getProfile().getCurrency(),
                credentialServiceController.getProvidersByCredentialIds(user.getId()))
                .convertFrom(accountServiceController.list(user.getId()));
        responseObserver.onNext(ListAccountsResponse.newBuilder().addAllAccounts(accounts).build());
        responseObserver.onCompleted();
    }
}
