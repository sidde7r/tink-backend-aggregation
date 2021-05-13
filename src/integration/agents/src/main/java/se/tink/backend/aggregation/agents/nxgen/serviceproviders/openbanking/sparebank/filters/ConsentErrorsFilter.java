package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.filters;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.ErrorMessages.CONSENT_REVOKED_MESSAGE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.ErrorMessages.SCA_REDIRECT_MESSAGE;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.exceptions.ConsentExpiredException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.exceptions.ConsentRevokedException;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class ConsentErrorsFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == 401
                && hasErrorMessageContaining(response, SCA_REDIRECT_MESSAGE)) {
            log.warn("[SpareBank] Unexpected consent expiration");
            throw new ConsentExpiredException();
        }
        if (response.getStatus() == 403
                && hasErrorMessageContaining(response, CONSENT_REVOKED_MESSAGE)) {
            log.warn("[SpareBank] Consent was revoked");
            throw new ConsentRevokedException();
        }

        return response;
    }

    private boolean hasErrorMessageContaining(HttpResponse response, String value) {
        if (!response.hasBody()) {
            return false;
        }
        return response.getBody(String.class).contains(value);
    }
}
