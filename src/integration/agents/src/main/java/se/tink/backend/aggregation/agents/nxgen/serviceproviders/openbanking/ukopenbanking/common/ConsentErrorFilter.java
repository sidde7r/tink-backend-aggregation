package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common;

import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.SessionKiller;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConsentValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ConsentErrorFilter extends Filter {

    private final PersistentStorage persistentStorage;

    public ConsentErrorFilter(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    @Override
    public HttpResponse handle(HttpRequest request)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(request);
        validateIfHasValidConsent(request, response);
        return response;
    }

    private void validateIfHasValidConsent(HttpRequest request, HttpResponse response) {
        if (OpenIdConsentValidator.hasValidConsent(response)) {
            return;
        }
        terminateSession(request, response);
    }

    private void terminateSession(HttpRequest request, HttpResponse response) {
        String exceptionMessage = getExceptionMessage(request, response);
        SessionException exception = SessionError.CONSENT_INVALID.exception(exceptionMessage);
        SessionKiller.cleanUpAndExpireSession(persistentStorage, exception);
    }

    private String getExceptionMessage(HttpRequest request, HttpResponse response) {
        String messagePattern =
                "[ConsentErrorFilter] The consent error occurred for path: `%s`,"
                        + " with HTTP status: `%s` and ErrorCodes:`%s`";
        String path = request.getUrl().toUri().getPath();
        int status = response.getStatus();
        List<String> errorCodes = response.getBody(ErrorResponse.class).getErrorCodes();
        return String.format(messagePattern, path, status, errorCodes);
    }
}
