package se.tink.sa.framework.mapper;

public interface ToDomainMapper<D, B> {

    default D mapToTransferModel(B source) {
        return mapToTransferModel(source, MappingContext.newInstance());
    }

    D mapToTransferModel(B source, MappingContext context);
}
