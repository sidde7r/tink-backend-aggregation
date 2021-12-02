package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.filter;

import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.LogTags.NO_ACTIVE_CONSENT;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public class BelfiusConsentErrorFilter extends Filter {

    private final PersistentStorage persistentStorage;
    private final Date sessionExpiryDate;

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        HttpResponse httpResponse = nextFilter(httpRequest);
        invalidateSessionOnNoActiveConsent(httpResponse);
        return httpResponse;
    }

    private void invalidateSessionOnNoActiveConsent(HttpResponse httpResponse) {
        if (httpResponse.getStatus() == 403
                && httpResponse.hasBody()
                && httpResponse.getBody(String.class).contains("no_active_consent")) {
            log.info(
                    "{} Consent invalid. Force to manual authentication. Error response: {}.\nSession expiry date: {}\nIs session expired prematurely: {}",
                    NO_ACTIVE_CONSENT,
                    httpResponse.getBody(String.class),
                    sessionExpiryDate,
                    SessionExpiryDateComparator.getSessionExpiryInfo(sessionExpiryDate));
            persistentStorage.clear();
            throw SessionError.SESSION_EXPIRED.exception(
                    "User\\System has deactivated the consent");
        }
    }
}
