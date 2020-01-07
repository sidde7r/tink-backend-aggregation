package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.filter;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.exceptions.errors.AgentRuntimeError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class RuntimeErrorFilter extends Filter {

    private static final ImmutableMap<Integer, AgentRuntimeError> errorMap =
            ImmutableMap.<Integer, AgentRuntimeError>builder()
                    .put(150, BankServiceError.NO_BANK_SERVICE)
                    .build();

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        try {
            return nextFilter(httpRequest);
        } catch (HttpResponseException e) {

            ErrorResponse response = e.getResponse().getBody(ErrorResponse.class);

            if (errorMap.containsKey(response.getErrorCode())) {
                throw errorMap.get(response.getErrorCode()).exception(e);
            }

            throw e;
        }
    }
}
