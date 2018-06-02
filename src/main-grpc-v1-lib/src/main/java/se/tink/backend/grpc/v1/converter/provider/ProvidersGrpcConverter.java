package se.tink.backend.grpc.v1.converter.provider;

import java.util.List;
import se.tink.backend.core.Provider;
import se.tink.grpc.v1.models.Providers;

public class ProvidersGrpcConverter {
    private CoreProviderToGrpcProviderConverter coreProviderToGrpcProviderConverter;

    public ProvidersGrpcConverter(
            CoreProviderToGrpcProviderConverter coreProviderToGrpcProviderConverter) {
        this.coreProviderToGrpcProviderConverter = coreProviderToGrpcProviderConverter;
    }

    public Providers convertFrom(List<Provider> input) {
        List<se.tink.grpc.v1.models.Provider> providers = coreProviderToGrpcProviderConverter.convertFrom(input);
        return Providers.newBuilder().addAllProvider(providers).build();
    }
}
