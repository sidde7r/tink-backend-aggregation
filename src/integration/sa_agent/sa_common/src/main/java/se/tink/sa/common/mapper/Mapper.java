package se.tink.sa.common.mapper;

public interface Mapper<D, S> {

    default D mapToTransferModel(S source) {
        return mapToTransferModel(source, MappingContext.newInstance());
    }

    D mapToTransferModel(S source, MappingContext context);
}
