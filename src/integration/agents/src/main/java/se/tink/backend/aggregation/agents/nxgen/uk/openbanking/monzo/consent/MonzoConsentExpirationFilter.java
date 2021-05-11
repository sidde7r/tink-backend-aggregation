package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.consent;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.SessionKiller;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.rpc.Response;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class MonzoConsentExpirationFilter extends Filter {

    private static final String CONSENT_SCA_EXPIRED_CODE = "forbidden.consent_sca_expired";

    private final PersistentStorage storage;

    public MonzoConsentExpirationFilter(PersistentStorage storage) {
        this.storage = storage;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (is403(response) && hasConsentExpiredCode(response)) {
            SessionKiller.cleanUpAndExpireSession(
                    storage,
                    SessionError.CONSENT_EXPIRED.exception(
                            "Consent expired. Expiring the session."));
        }

        return response;
    }

    private boolean is403(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_FORBIDDEN;
    }

    private boolean hasConsentExpiredCode(HttpResponse response) {
        return response.getBody(Response.class).hasCode(CONSENT_SCA_EXPIRED_CODE);
    }
}
