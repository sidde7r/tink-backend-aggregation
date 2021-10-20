package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.revolut.common.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.SessionKiller;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RevolutConsentAuthorisationErrorFilter extends Filter {

    private static final String UNAUTHORIZED = "UK.OBIE.Resource.Unauthorized";
    private final PersistentStorage persistentStorage;

    public RevolutConsentAuthorisationErrorFilter(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (is401(response) && hasConsentUnauthorizedCode(response)) {
            String msgErr =
                    String.format(
                            "[RevolutConsentAuthorisationErrorFilter] Invalid consent revoked by the bank with a message: %s",
                            response.getBody(ErrorResponse.class).getMessage());
            SessionKiller.cleanUpAndExpireSession(
                    persistentStorage, SessionError.CONSENT_INVALID.exception(msgErr));
        }

        return response;
    }

    private boolean hasConsentUnauthorizedCode(HttpResponse response) {
        return response.getBody(ErrorResponse.class).hasErrorCode(UNAUTHORIZED);
    }

    private boolean is401(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_UNAUTHORIZED;
    }
}
