package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.filters;

import se.tink.backend.aggregation.agents.exceptions.errors.AgentRuntimeError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public class ErrorFilter extends Filter {
    private final int httpCode;
    private final String errorCode;
    private final AgentRuntimeError runtimeError;

    public ErrorFilter(int httpCode, String errorCode, AgentRuntimeError runtimeError) {
        this.httpCode = httpCode;
        this.errorCode = errorCode;
        this.runtimeError = runtimeError;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == httpCode) {
            final ErrorResponse error = ErrorResponse.fromResponse(response);
            if (error.hasErrorCode(errorCode)) {
                throw runtimeError.exception();
            }
        }

        return response;
    }
}
