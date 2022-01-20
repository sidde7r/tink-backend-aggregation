package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.tesco.filter;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.SessionKiller;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.ConsentErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.tesco.rpc.Response;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

/**
 * Filter ordering applied with max value (filters are handled in a recursive way) and FilterPhase
 * to CUSTOM to ensure that TescoConsentExpirationFilter is visited before {@link
 * ConsentErrorFilter}.
 */
@FilterOrder(category = FilterPhases.CUSTOM, order = Integer.MAX_VALUE)
public class TescoConsentExpirationFilter extends Filter {

    private final PersistentStorage persistentStorage;

    public TescoConsentExpirationFilter(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        HttpResponse response = nextFilter(httpRequest);
        if (!response.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE)) {
            response.getHeaders()
                    .putSingle(
                            HttpHeaders.CONTENT_TYPE,
                            String.valueOf(MediaType.APPLICATION_JSON_TYPE));
        }
        if (is403(response) && hasConsentExpiredCode(response)) {
            SessionKiller.cleanUpAndExpireSession(
                    persistentStorage,
                    SessionError.CONSENT_EXPIRED.exception(
                            "Consent expired. Expiring the session."));
        }
        return response;
    }

    private boolean is403(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_FORBIDDEN;
    }

    private boolean hasConsentExpiredCode(HttpResponse response) {
        return response.getBody(Response.class).hasCode("UK.OBIE.Reauthenticate");
    }
}
