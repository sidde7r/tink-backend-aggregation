package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter;

import java.util.Objects;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
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
        try {
            return nextFilter(httpRequest);
        } catch (HttpResponseException e) {
            final HttpResponse response = e.getResponse();
            checkErrorResponseBodyType(response);
            throwIfServiceBlocked(response);
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e.getMessage());
        }
    }

    private void checkErrorResponseBodyType(HttpResponse response) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.getType())) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Invalid error response format : " + response.getBody(String.class));
        }
    }

    private void throwIfServiceBlocked(HttpResponse response) {
        TppErrorResponse errorResponses = response.getBody(TppErrorResponse.class);
        if (response.getStatus() == HttpStatus.SC_FORBIDDEN
                && errorResponses.isAnyServiceBlocked()) {
            if (credentials.getType() == CredentialsTypes.PASSWORD) {
                credentials.setType(CredentialsTypes.MOBILE_BANKID);
                systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, false);
            }
            final String errorMessage =
                    Objects.requireNonNull(
                                    errorResponses.getTppMessages().stream()
                                            .findFirst()
                                            .orElse(null))
                            .getText();
            throw SessionError.CONSENT_INVALID.exception(errorMessage);
        }
    }
}
