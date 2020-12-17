package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.rpc.TppErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class ServiceBlockedFilter extends Filter {

    private final SystemUpdater systemUpdater;
    private final Credentials credentials;

    public ServiceBlockedFilter(SystemUpdater systemUpdater, Credentials credentials) {
        this.systemUpdater = systemUpdater;
        this.credentials = credentials;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        // this is due to the response sometimes can be 503 with a html body.
        // If that is the case, we will throw a BankServiceException to inform the user and suggest
        // them to try again
        if (response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }

        TppErrorResponse body = response.getBody(TppErrorResponse.class);
        if (response.getStatus() == HttpStatus.SC_FORBIDDEN && body.isAnyServiceBlocked()) {
            if (credentials.getType() == CredentialsTypes.PASSWORD) {
                credentials.setType(CredentialsTypes.MOBILE_BANKID);
                systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, false);
            }
            throw BankServiceError.CONSENT_EXPIRED.exception();
        }
        return response;
    }
}
