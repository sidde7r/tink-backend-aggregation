package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold;

import java.net.URI;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class ExternalApiCallResult<T> {

    private T result;
    private int statusCode;
    private URI redirectLocation;

    public static <T> ExternalApiCallResult<T> of(T result, int status) {
        return ExternalApiCallResult.<T>builder().result(result).statusCode(status).build();
    }

    public static <T> ExternalApiCallResult<T> of(T result, int status, URI redirectLocation) {
        return ExternalApiCallResult.<T>builder()
                .result(result)
                .statusCode(status)
                .redirectLocation(redirectLocation)
                .build();
    }

    public boolean is2xxSuccess() {
        return statusCodeSeries() == 2;
    }

    public boolean is3xxRedirect() {
        return statusCodeSeries() == 3;
    }

    private int statusCodeSeries() {
        return statusCode / 100;
    }
}
