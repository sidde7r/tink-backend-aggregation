package se.tink.backend.aggregation.nxgen.scaffold;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public abstract class SimpleExternalApiCall<T, R> implements ExternalApiCall<T, R> {

    private final TinkHttpClient httpClient;

    protected SimpleExternalApiCall(TinkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public ExternalApiCallResult<R> execute(T arg) {
        return Optional.of(arg)
                .map(this::prepareRequest)
                .map(this::executeHttpCall)
                .map(this::parseResponse)
                .orElseThrow(IllegalArgumentException::new);
    }

    protected abstract HttpRequest prepareRequest(T arg);

    protected abstract ExternalApiCallResult<R> parseResponse(HttpResponse httpResponse);

    private HttpResponse executeHttpCall(HttpRequest httpRequest) {
        return httpClient.request(HttpResponse.class, httpRequest);
    }
}
