package se.tink.backend.aggregation.nxgen.scaffold;

public interface ExternalApiCall<T, R> {

    ExternalApiCallResult<R> execute(T arg);
}
