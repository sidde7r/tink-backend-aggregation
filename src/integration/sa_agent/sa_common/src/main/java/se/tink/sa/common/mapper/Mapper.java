package se.tink.sa.common.mapper;

public interface Mapper<D, S> {

    default D map(S source) {
        return map(source, MappingContext.newInstance());
    }

    D map(S source, MappingContext mappingContext);
}
