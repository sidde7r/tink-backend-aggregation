package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.filter;

import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.rpc.TppMessageEntity;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SamlinkSessionErrorFilter extends Filter {
    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        try {
            return nextFilter(httpRequest);
        } catch (HttpResponseException e) {
            final HttpResponse httpResponse = e.getResponse();
            throwIfConsentError(httpResponse);
            // Throw BankServiceError for other error responses to avoid user to do manual
            // authentication
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e.getMessage());
        }
    }

    public static void throwIfConsentError(HttpResponse httpResponse) {
        checkErrorResponseBodyType(httpResponse);
        final List<TppMessageEntity> errors =
                httpResponse.getBody(ErrorResponse.class).getTppMessages();
        if (errors.stream().anyMatch(TppMessageEntity::isConsentExpired)) {
            throw SessionError.CONSENT_EXPIRED.exception("Cause: Consent is expired");
        }
    }

    private static void checkErrorResponseBodyType(HttpResponse httpResponse) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(httpResponse.getType())) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Invalid error response format : " + httpResponse.getBody(String.class));
        }
    }
}
