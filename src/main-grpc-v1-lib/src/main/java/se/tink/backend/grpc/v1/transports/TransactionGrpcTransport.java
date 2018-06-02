package se.tink.backend.grpc.v1.transports;

import com.google.common.collect.BiMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.grpc.stub.StreamObserver;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.common.exceptions.LockException;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.grpc.v1.converter.transaction.CoreTransactionToGrpcTransactionConverter;
import se.tink.backend.grpc.v1.converter.transaction.QueryTransactionRequestConverter;
import se.tink.backend.grpc.v1.converter.transaction.QueryTransactionResponseConverter;
import se.tink.backend.grpc.v1.converter.transaction.SuggestTransactionsResponseConverter;
import se.tink.backend.grpc.v1.converter.transaction.UpdateTransactionRequestConverter;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.TransactionServiceController;
import se.tink.backend.rpc.SearchResponse;
import se.tink.grpc.v1.rpc.CategorizeTransactionsRequest;
import se.tink.grpc.v1.rpc.CategorizeTransactionsResponse;
import se.tink.grpc.v1.rpc.GetSimilarTransactionsRequest;
import se.tink.grpc.v1.rpc.GetSimilarTransactionsResponse;
import se.tink.grpc.v1.rpc.GetTransactionRequest;
import se.tink.grpc.v1.rpc.GetTransactionResponse;
import se.tink.grpc.v1.rpc.QueryTransactionsRequest;
import se.tink.grpc.v1.rpc.QueryTransactionsResponse;
import se.tink.grpc.v1.rpc.SuggestTransactionsRequest;
import se.tink.grpc.v1.rpc.SuggestTransactionsResponse;
import se.tink.grpc.v1.rpc.UpdateTransactionRequest;
import se.tink.grpc.v1.rpc.UpdateTransactionResponse;
import se.tink.grpc.v1.services.TransactionServiceGrpc;

public class TransactionGrpcTransport extends TransactionServiceGrpc.TransactionServiceImplBase {
    private final TransactionServiceController transactionServiceController;

    private final BiMap<String, String> categoryCodeById;

    @Inject
    public TransactionGrpcTransport(TransactionServiceController transactionServiceController,
            @Named("categoryCodeById") BiMap<String, String> categoryCodeById) {
        this.transactionServiceController = transactionServiceController;
        this.categoryCodeById = categoryCodeById;
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.TRANSACTIONS_READ)
    public void queryTransactions(QueryTransactionsRequest queryTransactionsRequest,
            StreamObserver<QueryTransactionsResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        SearchQuery searchQuery = new QueryTransactionRequestConverter(categoryCodeById.inverse())
                .convertFrom(queryTransactionsRequest);
        SearchResponse searchResponse = transactionServiceController.searchTransactions(user, searchQuery);

        responseObserver.onNext(new QueryTransactionResponseConverter(user.getProfile().getCurrency(),
                categoryCodeById).convertFrom(searchResponse));
        responseObserver.onCompleted();
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.TRANSACTIONS_READ)
    public void getTransaction(GetTransactionRequest getTransactionRequest,
            StreamObserver<GetTransactionResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        try {
            Transaction transaction = transactionServiceController
                    .getTransaction(user, getTransactionRequest.getTransactionId());
            responseObserver.onNext(GetTransactionResponse.newBuilder()
                    .setTransaction(
                            new CoreTransactionToGrpcTransactionConverter(user.getProfile().getCurrency(),
                                    categoryCodeById).convertFrom(transaction))
                    .build());
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            throw ApiError.Transactions.NOT_FOUND.exception();
        }
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.TRANSACTIONS_WRITE)
    public void updateTransaction(UpdateTransactionRequest updateTransactionRequest,
            StreamObserver<UpdateTransactionResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();
        try {
            Transaction transaction = transactionServiceController
                    .updateTransactions(user.getId(), updateTransactionRequest.getTransactionId(),
                            new UpdateTransactionRequestConverter().convertFrom(updateTransactionRequest),
                            true, true);
            streamObserver.onNext(UpdateTransactionResponse.newBuilder()
                    .setTransaction(
                            new CoreTransactionToGrpcTransactionConverter(user.getProfile().getCurrency(),
                                    categoryCodeById).convertFrom(transaction))
                    .build());
            streamObserver.onCompleted();
        } catch (NoSuchElementException e) {
            throw ApiError.Transactions.NOT_FOUND.exception();
        }
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.TRANSACTIONS_CATEGORIZE)
    public void categorizeTransactions(CategorizeTransactionsRequest request,
            StreamObserver<CategorizeTransactionsResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        String categoryCode = categoryCodeById.inverse().get(request.getCategoryCode());
        if (categoryCode == null) {
            throw ApiError.Transactions.UNKNOWN_CATEGORY.exception();
        }

        List<Transaction> transactions = transactionServiceController.categorize(user,
                Collections.singletonList(new se.tink.backend.rpc.CategorizeTransactionsRequest(categoryCode,
                        request.getTransactionIdsList())));
        streamObserver
                .onNext(CategorizeTransactionsResponse.newBuilder().addAllTransactions(
                        new CoreTransactionToGrpcTransactionConverter(user.getProfile().getCurrency(),
                                categoryCodeById).convertFrom(transactions))
                        .build());
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.TRANSACTIONS_READ)
    public void getSimilarTransactions(GetSimilarTransactionsRequest getSimilarTransactionsRequest,
            StreamObserver<GetSimilarTransactionsResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        List<Transaction> transactions;
        if (getSimilarTransactionsRequest.hasCategoryCode()) {
            String categoryCode = categoryCodeById.inverse()
                    .get(getSimilarTransactionsRequest.getCategoryCode().getValue());
            if (categoryCode == null) {
                throw ApiError.Transactions.UNKNOWN_CATEGORY.exception();

            }
            transactions = transactionServiceController.findSimilarTransactions(user,
                    getSimilarTransactionsRequest.getTransactionId(),
                    categoryCode,
                    false);
        } else {
            transactions = transactionServiceController
                    .findSimilarTransactions(user, getSimilarTransactionsRequest.getTransactionId(), false);
        }
        responseObserver.onNext(GetSimilarTransactionsResponse.newBuilder()
                .addAllTransactions(
                        new CoreTransactionToGrpcTransactionConverter(user.getProfile().getCurrency(),
                                categoryCodeById).convertFrom(transactions))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.TRANSACTIONS_READ)
    public void suggestTransactions(SuggestTransactionsRequest suggestTransactionsRequest,
            StreamObserver<SuggestTransactionsResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        try {
            se.tink.backend.rpc.SuggestTransactionsResponse suggestTransactions = transactionServiceController
                    .suggest(user, suggestTransactionsRequest.getNumberOfClusters(),
                            suggestTransactionsRequest.getEvaluateEverything());
            streamObserver.onNext(new SuggestTransactionsResponseConverter(user.getProfile().getCurrency(),
                    categoryCodeById).convertFrom(suggestTransactions));
            streamObserver.onCompleted();
        } catch (LockException e) {
            throw ApiError.Transactions.UNAVAILABLE.exception();
        }
    }
}
