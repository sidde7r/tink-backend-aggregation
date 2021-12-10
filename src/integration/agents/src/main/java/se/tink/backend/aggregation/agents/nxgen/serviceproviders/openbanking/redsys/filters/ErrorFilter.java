package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.filters;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class ErrorFilter extends Filter {
    private final int httpCode;
    private final String errorCode;
    private final AgentError runtimeError;

    public ErrorFilter(int httpCode, String errorCode, AgentError runtimeError) {
        this.httpCode = httpCode;
        this.errorCode = errorCode;
        this.runtimeError = runtimeError;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        try {
            HttpResponse response = nextFilter(httpRequest);

            if (response.getStatus() == httpCode) {
                final ErrorResponse error = ErrorResponse.fromResponse(response);
                if (error.hasErrorCode(errorCode)) {
                    throw runtimeError.exception();
                }
            }

            return response;
        } catch (HttpClientException ex) {
            if (isRedsysTemporaryUnavailable(ex)) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            throw ex;
        }
    }

    private boolean isRedsysTemporaryUnavailable(HttpClientException ex) {
        return ex.getMessage().contains("psd2.redsys.es:443 failed to respond");
    }
}
