package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold;

public interface ExternalApiCall<T, R> {

    ExternalApiCallResult<R> execute(T arg);
}
