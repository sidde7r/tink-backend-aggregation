package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ReAuthenticateFilter extends Filter {

    private static final String NEEDS_AUTHENTICATE = "UK.OBIE.ReAuthenticate";

    private final PersistentStorage persistentStorage;

    public ReAuthenticateFilter(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        String errorResponse = response.getBody(String.class);
        if (response.getStatus() == HttpStatus.SC_FORBIDDEN
                && errorResponse.contains(NEEDS_AUTHENTICATE)) {

            // The response indicates that the user needs to re-authenticate, even if we have a
            // valid access token. In order to enforce this, the token is removed from storage to
            // prevent our framework to use the stored access token and prevent it from refreshing
            // it.
            persistentStorage.remove(
                    UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCESS_TOKEN);
            throw SessionError.CONSENT_REVOKED.exception(
                    "User needs to re-authenticate, removed token from storage.");
        }
        return response;
    }
}
