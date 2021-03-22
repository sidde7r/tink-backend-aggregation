package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.transactions.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.transactions.ErrorTppMessage;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class DeutscheKnownErrorsFilter extends Filter {

    private static final Map<String, AgentError> KNOWN_ERROR_MESSAGES =
            ImmutableMap.<String, AgentError>builder()
                    .put(
                            "Parameters submitted by TPP are missing or invalid : psu_id should be 10 characters",
                            LoginError.INCORRECT_CREDENTIALS)
                    .put(
                            "Parameters submitted by TPP are missing or invalid : psu_id should be 8 characters",
                            LoginError.INCORRECT_CREDENTIALS)
                    .put(
                            "The system has encountered a Technical/Server Error. Hence cannot process the request at this time. Please try again after sometime.",
                            BankServiceError.BANK_SIDE_FAILURE)
                    .build();

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (!response.hasBody()) {
            return response;
        }

        ErrorResponse errorResponse = getBodyAsExpectedType(response);
        if (errorResponse != null) {
            List<String> errorTextsInResponse =
                    errorResponse.getTppMessages().stream()
                            .map(ErrorTppMessage::getText)
                            .collect(Collectors.toList());

            for (String text : errorTextsInResponse) {
                if (KNOWN_ERROR_MESSAGES.containsKey(text)) {
                    throw KNOWN_ERROR_MESSAGES.get(text).exception(text);
                }
            }
        }

        return response;
    }

    private ErrorResponse getBodyAsExpectedType(HttpResponse response) {
        try {
            return response.getBody(ErrorResponse.class);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
