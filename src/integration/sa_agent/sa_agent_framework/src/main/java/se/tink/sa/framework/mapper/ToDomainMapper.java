package se.tink.sa.framework.mapper;

public interface ToDomainMapper<D, S> {

    default D mapToTransferModel(S source) {
        return mapToTransferModel(source, MappingContext.newInstance());
    }

    D mapToTransferModel(S source, MappingContext context);
}
