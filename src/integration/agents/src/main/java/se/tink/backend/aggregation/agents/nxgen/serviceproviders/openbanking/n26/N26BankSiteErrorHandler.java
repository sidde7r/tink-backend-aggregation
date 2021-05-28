package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26;

import com.google.common.collect.Sets;
import java.util.Set;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class N26BankSiteErrorHandler extends DefaultResponseStatusHandler {

    private static final Set<String> bankSiteErrorBodyMessageParts =
            Sets.newHashSet(
                    "upstream request timeout",
                    "MIME media type text/plain was not found",
                    "upstream connect error");

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (httpResponse.getStatus() >= 500) {
            String response = httpResponse.getBody(String.class);
            bankSiteErrorBodyMessageParts.stream()
                    .filter(v -> response.contains(v))
                    .findAny()
                    .ifPresent(this::throwBankSiteException);
        }
        super.handleResponse(httpRequest, httpResponse);
    }

    private void throwBankSiteException(String message) {
        throw BankServiceError.BANK_SIDE_FAILURE.exception(message);
    }
}
