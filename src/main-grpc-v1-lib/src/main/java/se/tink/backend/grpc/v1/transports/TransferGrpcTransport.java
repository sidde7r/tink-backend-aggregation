package se.tink.backend.grpc.v1.transports;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.common.exceptions.DuplicateException;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferDestination;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.converter.account.CoreAccountToGrpcAccountConverter;
import se.tink.backend.grpc.v1.converter.provider.CoreImageUrlsToGrpcImagesConverter;
import se.tink.backend.grpc.v1.converter.transfer.CoreGiroLookupEntityToGrpcConverter;
import se.tink.backend.grpc.v1.converter.transfer.CoreSignableOperationToGrpcSignableOperationConverter;
import se.tink.backend.grpc.v1.converter.transfer.CoreTransferDestinationToGrpcTransferDestinationConverter;
import se.tink.backend.grpc.v1.converter.transfer.CoreTransferToGrpcTransferConverter;
import se.tink.backend.grpc.v1.converter.transfer.CreateTransferRequestToCoreTransferConverter;
import se.tink.backend.grpc.v1.converter.transfer.DestinationsPerAccountConverter;
import se.tink.backend.grpc.v1.converter.transfer.UpdateTransferRequestGrpcToCoreConverter;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.CredentialServiceController;
import se.tink.backend.main.controllers.TransferServiceController;
import se.tink.backend.main.validators.exception.TransferNotFoundException;
import se.tink.backend.rpc.AccountListResponse;
import se.tink.backend.rpc.GiroLookupEntity;
import se.tink.backend.rpc.transfer.GetTransferDestinationsPerAccountCommand;
import se.tink.backend.rpc.transfer.TransferDestinationsPerAccountResult;
import se.tink.grpc.v1.rpc.ClearingLookupRequest;
import se.tink.grpc.v1.rpc.ClearingLookupResponse;
import se.tink.grpc.v1.rpc.CreateTransferDestinationRequest;
import se.tink.grpc.v1.rpc.CreateTransferDestinationResponse;
import se.tink.grpc.v1.rpc.CreateTransferRequest;
import se.tink.grpc.v1.rpc.CreateTransferResponse;
import se.tink.grpc.v1.rpc.GetAccountsForTransferRequest;
import se.tink.grpc.v1.rpc.GetAccountsForTransferResponse;
import se.tink.grpc.v1.rpc.GetTransferDestinationsRequest;
import se.tink.grpc.v1.rpc.GetTransferDestinationsResponse;
import se.tink.grpc.v1.rpc.GiroLookupRequest;
import se.tink.grpc.v1.rpc.GiroLookupResponse;
import se.tink.grpc.v1.rpc.TransferGetRequest;
import se.tink.grpc.v1.rpc.TransferGetResponse;
import se.tink.grpc.v1.rpc.TransferListRequest;
import se.tink.grpc.v1.rpc.TransferListResponse;
import se.tink.grpc.v1.rpc.UpdateTransferRequest;
import se.tink.grpc.v1.rpc.UpdateTransferResponse;
import se.tink.grpc.v1.services.TransferServiceGrpc;

public class TransferGrpcTransport extends TransferServiceGrpc.TransferServiceImplBase {
    private final TransferServiceController transferServiceController;
    private final CredentialServiceController credentialServiceController;

    @Inject
    public TransferGrpcTransport(TransferServiceController transferServiceController, CredentialServiceController credentialServiceController) {
        this.transferServiceController = transferServiceController;
        this.credentialServiceController = credentialServiceController;
    }

    @Override
    @Authenticated(requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE,
            scopes = OAuth2AuthorizationScopeTypes.TRANSFER_EXECUTE)
    public void createTransfer(CreateTransferRequest createTransferRequest,
            StreamObserver<CreateTransferResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            SignableOperation signableOperation = transferServiceController.create(authenticationContext.getUser(),
                    new CreateTransferRequestToCoreTransferConverter().convertFrom(createTransferRequest),
                    authenticationContext.getRemoteAddress());

            CreateTransferResponse response = CreateTransferResponse.newBuilder()
                    .setSignableOperation(
                            new CoreSignableOperationToGrpcSignableOperationConverter().convertFrom(signableOperation))
                    .build();

            streamObserver.onNext(response);
            streamObserver.onCompleted();
        } catch (UnsupportedOperationException e) {
            throw ApiError.Transfers.SERVICE_TEMPORARY_DISABLED.withCause(e).exception();
        } catch (TransferNotFoundException e) {
            throw ApiError.Transfers.NOT_FOUND.withCause(e).exception();
        }
    }

    @Override
    @Authenticated(requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE)
    public void updateTransfer(UpdateTransferRequest updateTransferRequest,
            StreamObserver<UpdateTransferResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            SignableOperation signableOperation = transferServiceController.update(authenticationContext.getUser(),
                    updateTransferRequest.getTransferId(),
                    new UpdateTransferRequestGrpcToCoreConverter().convertFrom(updateTransferRequest),
                    authenticationContext.getRemoteAddress());

            UpdateTransferResponse response = UpdateTransferResponse.newBuilder()
                    .setSignableOperation(
                            new CoreSignableOperationToGrpcSignableOperationConverter().convertFrom(signableOperation))
                    .build();

            streamObserver.onNext(response);
            streamObserver.onCompleted();
        } catch (UnsupportedOperationException e) {
            throw ApiError.Transfers.SERVICE_TEMPORARY_DISABLED.withCause(e).exception();
        } catch (TransferNotFoundException e) {
            throw ApiError.Transfers.NOT_FOUND.withCause(e).exception();
        }
    }

    @Override
    @Authenticated(requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE)
    public void getTransfer(TransferGetRequest transferGetRequest, StreamObserver<TransferGetResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            Transfer transfer = transferServiceController
                    .get(authenticationContext.getUser(), transferGetRequest.getTransferId());

            TransferGetResponse response = TransferGetResponse.newBuilder()
                    .setTransfer(new CoreTransferToGrpcTransferConverter().convertFrom(transfer))
                    .build();

            streamObserver.onNext(response);
            streamObserver.onCompleted();
        } catch (UnsupportedOperationException e) {
            throw ApiError.Transfers.SERVICE_TEMPORARY_DISABLED.withCause(e).exception();
        } catch (NoSuchElementException e) {
            throw ApiError.Transfers.NOT_FOUND.withCause(e).exception();
        } catch (IllegalStateException e) {
            throw ApiError.Transfers.PERMISSION_DENIED.withCause(e).exception();
        }
    }

    @Override
    @Authenticated(requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE)
    public void listTransfers(TransferListRequest transferListRequest,
            StreamObserver<TransferListResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            List<Transfer> transfers = transferServiceController.list(authenticationContext.getUser(),
                    EnumMappers.CORE_TRANSFER_TYPE_TO_GRPC_MAP.inverse().get(transferListRequest.getType()));

            TransferListResponse response = TransferListResponse.newBuilder()
                    .addAllTransfers(new CoreTransferToGrpcTransferConverter().convertFrom(transfers))
                    .build();

            streamObserver.onNext(response);
            streamObserver.onCompleted();
        } catch (UnsupportedOperationException e) {
            throw ApiError.Transfers.SERVICE_TEMPORARY_DISABLED.withCause(e).exception();
        }
    }

    @Override
    @Authenticated(requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE)
    public void createTransferDestination(CreateTransferDestinationRequest createTransferDestinationRequest,
            StreamObserver<CreateTransferDestinationResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        try {
            TransferDestination transferDestination = transferServiceController.createDestination(user,
                    URI.create(createTransferDestinationRequest.getUri()), createTransferDestinationRequest.getName());

            CreateTransferDestinationResponse response = CreateTransferDestinationResponse.newBuilder()
                    .setDestination(new CoreTransferDestinationToGrpcTransferDestinationConverter(
                            user.getProfile().getCurrency()).convertFrom(transferDestination))
                    .build();

            streamObserver.onNext(response);
            streamObserver.onCompleted();
        } catch (DuplicateException e) {
            throw ApiError.Transfers.Destinations.ALREADY_EXISTS.withCause(e).withInfoSeverity().exception();
        } catch (UnsupportedOperationException e) {
            throw ApiError.Transfers.SERVICE_TEMPORARY_DISABLED.withCause(e).exception();
        }
    }

    @Override
    @Authenticated(requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE)
    public void lookupGiro(GiroLookupRequest giroLookupRequest, StreamObserver<GiroLookupResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            List<GiroLookupEntity> giroLookupEntities = transferServiceController
                    .giroLookup(authenticationContext.getUser(), giroLookupRequest.getGiroNumber());

            GiroLookupResponse response = GiroLookupResponse.newBuilder()
                    .addAllGiroEntities(new CoreGiroLookupEntityToGrpcConverter().convertFrom(giroLookupEntities))
                    .build();
            streamObserver.onNext(response);
            streamObserver.onCompleted();
        } catch (NoSuchElementException e) {
            throw ApiError.Transfers.Giro.NOT_FOUND.withCause(e).exception();
        } catch (IllegalArgumentException e) {
            throw ApiError.Transfers.Giro.INVALID_GIRO.withInfoSeverity().withCause(e).exception();
        } catch (UnsupportedOperationException e) {
            throw ApiError.Transfers.SERVICE_TEMPORARY_DISABLED.withCause(e).exception();
        }
    }

    @Override
    @Authenticated(requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE)
    public void lookupClearing(ClearingLookupRequest clearingLookupRequest,
            StreamObserver<ClearingLookupResponse> streamObserver) {

        se.tink.backend.rpc.ClearingLookupResponse clearingLookupResponse = transferServiceController
                .clearingLookup(clearingLookupRequest.getClearingNumber());

        ClearingLookupResponse response = ClearingLookupResponse.newBuilder()
                .setBankDisplayName(clearingLookupResponse.getBankDisplayName())
                .setImages(new CoreImageUrlsToGrpcImagesConverter().convertFrom(clearingLookupResponse.getImages()))
                .build();

        streamObserver.onNext(response);
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated(requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE)
    public void getAccountsForTransfer(GetAccountsForTransferRequest request,
            StreamObserver<GetAccountsForTransferResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        try {
            AccountListResponse accountListResponse = transferServiceController
                    .getSourceAccountsForTransfer(user, request.getTransferId());
            CoreAccountToGrpcAccountConverter converter = new CoreAccountToGrpcAccountConverter(
                    user.getProfile().getCurrency(),
                    credentialServiceController.getProvidersByCredentialIds(user.getId()));

            GetAccountsForTransferResponse response = GetAccountsForTransferResponse.newBuilder()
                    .addAllAccount(converter.convertFrom(accountListResponse.getAccounts()))
                    .build();

            streamObserver.onNext(response);
            streamObserver.onCompleted();
        } catch (UnsupportedOperationException e) {
            throw ApiError.Transfers.SERVICE_TEMPORARY_DISABLED.withCause(e).exception();
        } catch (NoSuchElementException e) {
            throw ApiError.Transfers.NOT_FOUND.withCause(e).exception();
        } catch (IllegalStateException e) {
            throw ApiError.Transfers.PERMISSION_DENIED.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void getTransferDestinations(GetTransferDestinationsRequest request,
            StreamObserver<GetTransferDestinationsResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();
        GetTransferDestinationsPerAccountCommand command = new GetTransferDestinationsPerAccountCommand(user);
        List<TransferDestinationsPerAccountResult> list =  transferServiceController.getTransferDestinationsPerAccount(command);
        DestinationsPerAccountConverter converter = new DestinationsPerAccountConverter(user.getProfile().getCurrency());

        GetTransferDestinationsResponse response = GetTransferDestinationsResponse.newBuilder()
                .addAllDestinationsPerAccount(converter.convertFrom(list))
                .build();

        streamObserver.onNext(response);
        streamObserver.onCompleted();
    }
}
