package se.tink.backend.grpc.v1.transports;

import com.google.common.collect.Lists;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.converter.provider.CoreProviderToGrpcProviderConverter;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.ProviderServiceController;
import se.tink.backend.rpc.GetProvidersByDeviceCommand;
import se.tink.backend.rpc.ProviderListResponse;
import se.tink.grpc.v1.rpc.ListProvidersResponse;
import se.tink.grpc.v1.rpc.ProviderListRequest;
import se.tink.grpc.v1.rpc.ProviderSuggestRequest;
import se.tink.grpc.v1.rpc.ProvidersByDeviceRequest;
import se.tink.grpc.v1.services.ProviderServiceGrpc;

public class ProviderGrpcTransport extends ProviderServiceGrpc.ProviderServiceImplBase {
    private ProviderServiceController providerServiceController;

    @Inject
    public ProviderGrpcTransport(ProviderServiceController providerServiceController) {
        this.providerServiceController = providerServiceController;
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.PROVIDERS_READ)
    public void listProviders(
            ProviderListRequest request, StreamObserver<ListProvidersResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        List<Provider> providers = providerServiceController.list(user.getId(), user.getProfile().getMarket(),
                EnumMappers.CORE_PROVIDER_CAPABILITY_TO_GRPC_MAP.inverse().get(request.getCapability()));

        CoreProviderToGrpcProviderConverter converter = new CoreProviderToGrpcProviderConverter();
        responseObserver
                .onNext(ListProvidersResponse.newBuilder().addAllProviders(converter.convertFrom(providers)).build());
        responseObserver.onCompleted();
    }

    @Override
    @Authenticated(required = false)
    public void listProvidersByDevice(ProvidersByDeviceRequest request,
            StreamObserver<ListProvidersResponse> streamObserver) {
        List<Provider> providers = providerServiceController
                .list(new GetProvidersByDeviceCommand(request.getDeviceId(), request.getMarketCode()));
        CoreProviderToGrpcProviderConverter providerConverter = new CoreProviderToGrpcProviderConverter();

        streamObserver
                .onNext(ListProvidersResponse.newBuilder().addAllProviders(providerConverter.convertFrom(providers))
                        .build());
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated()
    public void suggest(ProviderSuggestRequest request, StreamObserver<ListProvidersResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();
        Set<Provider> providersSet = providerServiceController.suggest(user);
        List<Provider> providers = Lists.newArrayList(providersSet);

        CoreProviderToGrpcProviderConverter converter = new CoreProviderToGrpcProviderConverter();
        responseObserver.onNext(ListProvidersResponse.newBuilder().addAllProviders(converter.convertFrom(providers)).build());
        responseObserver.onCompleted();
    }
}
