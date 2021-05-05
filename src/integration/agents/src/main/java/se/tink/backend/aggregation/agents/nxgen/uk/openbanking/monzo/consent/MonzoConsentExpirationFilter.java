package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.consent;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.SessionKiller;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class MonzoConsentExpirationFilter extends Filter {

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
                    storage, "Consent expired. Expiring the session.");
        }

        return response;
    }

    private boolean is403(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_FORBIDDEN;
    }

    private boolean hasConsentExpiredCode(HttpResponse response) {
        return response.getBody(ErrorResponse.class).hasErrorCode("forbidden.consent_sca_expired");
    }
}
