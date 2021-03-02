package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common;

import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConsentValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class InvalidConsentErrorFilter extends Filter {

    private static final String MESSAGE_PATTERN =
            "The consentId has been expired for path `%s` with HTTP status `%s` and ErrorCodes `%s`";

    private final PersistentStorage persistentStorage;

    public InvalidConsentErrorFilter(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (OpenIdConsentValidator.hasInvalidConsent(response)) {
            persistentStorage.put(
                    OpenIdConstants.PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID,
                    OpenIdAuthenticatorConstants.UNSPECIFIED_CONSENT_ID);
            throw SessionError.CONSENT_EXPIRED.exception(
                    String.format(
                            MESSAGE_PATTERN,
                            response.getStatus(),
                            httpRequest.getUrl().toUri().getPath(),
                            response.getBody(ErrorResponse.class).getErrorCodes()));
        }
        return response;
    }
}
