package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.filter;

import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.entities.TppMessageEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.utils.TriodosUtils;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class TriodosSessionErrorFilter extends Filter {
    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        try {
            return nextFilter(httpRequest);
        } catch (HttpResponseException e) {
            final HttpResponse httpResponse = e.getResponse();
            TriodosUtils.checkErrorResponseBodyType(httpResponse);

            final List<TppMessageEntity> errors =
                    httpResponse.getBody(ErrorResponse.class).getTppMessages();
            throwIfConsentError(errors);
            throwIfTokenError(errors);

            // Throw BankServiceError for other error responses to avoid user to do manual
            // authentication
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e.getMessage());
        }
    }

    private void throwIfTokenError(List<TppMessageEntity> errors) {
        if (errors.stream().anyMatch(TppMessageEntity::isTokenInvalid)) {
            throw SessionError.SESSION_EXPIRED.exception("Cause: Token invalid or expired");
        }
    }

    private void throwIfConsentError(List<TppMessageEntity> errors) {
        if (errors.stream().anyMatch(TppMessageEntity::isConsentInvalid)) {
            throw SessionError.SESSION_EXPIRED.exception("Cause: Consent invalid or expired");
        }
    }
}
