package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.rpc.filter;

import java.util.Objects;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.rpc.NordeaResponse;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.LogResponseFilter;
import se.tink.backend.agents.rpc.Credentials;

public class NordeaV17LogResponseFilter extends LogResponseFilter {
    private static final LogTag FOUND_CREDIT_CARD_CONTINUE_KEY_LOG_TAG = LogTag.from("#http_response_found_credit_card_continue_key_log_tag");
    private static final Pattern CONTINUE_KEY_PATTERN = Pattern.compile("^.*(continueKey|continuationKey).*$",
            Pattern.CASE_INSENSITIVE);

    public NordeaV17LogResponseFilter(Class<? extends NordeaResponse> responseModel) {
        super(responseModel);
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        String body = response.getBody(String.class);
        LogTag logTag = selectLogTag(body);

        log.infoExtraLong(String.format("Response (%s): %s", responseModel.getSimpleName(), body), logTag);

        return response;
    }

    // Temporary solution to find out if CreditCardTransactionsResponse can contain "continueKey" or "continuationKey"
    // TODO: Remove this when we either find the key or feel confident enough that it doesn't exist
    private LogTag selectLogTag(String body) {
        return (Objects.equals(responseModel, CreditCardTransactionsResponse.class) &&
                CONTINUE_KEY_PATTERN.matcher(body).matches()) ? FOUND_CREDIT_CARD_CONTINUE_KEY_LOG_TAG : logTag;
    }
}
