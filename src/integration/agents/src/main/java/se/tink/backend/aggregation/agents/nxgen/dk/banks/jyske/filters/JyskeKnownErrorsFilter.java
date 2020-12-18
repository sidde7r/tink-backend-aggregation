package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.filters;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class JyskeKnownErrorsFilter extends Filter {
    private static Logger log = LoggerFactory.getLogger(JyskeKnownErrorsFilter.class);

    private static final String MUST_REGISTER_MESSAGE =
            "Du skal tilmelde dig mobilbanken i Jyske Netbank, inden du kan logge på.";
    private static final String NO_ACTIVE_APPS_MESSAGE = "Ingen aktive nøgleapps";
    private static final String USER_ID_BLOCKED_MESSAGE =
            "Dit bruger-id er spærret hos NemID. Kontakt support.";
    private static final String INCORRECT_CREDENTIALS_MESSAGE = "Forkert bruger-id eller mobilkode";
    private static final String COULD_NOT_RETRIEVE_MESSAGE =
            "Kunne ikke hente kontobevægelser - Prøv igen senere.";
    private static final String ACCESS_TO_BANK_BLOCKED =
            "Din adgang til mobilbanken er spærret. Åbn den igen i Jyske Netbank.";
    private static final String SIDSTE_CHANCE = "Sidste chance";

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse httpResponse = nextFilter(httpRequest);

        // Only interested in errors with body
        if (httpResponse.getStatus() < 400 || !httpResponse.hasBody()) {
            return httpResponse;
        }

        ErrorResponse errorResponse;
        try {
            errorResponse = httpResponse.getBody(ErrorResponse.class);
        } catch (HttpClientException hce) {
            // Could not parse as expected body, meaning this is definitely not known exception
            return httpResponse;
        }

        String internalMessage = errorResponse.toString();
        log.info(internalMessage);

        // And in errors with error message, otherwise switch fails
        // Based on known error messages only, as it is possible to receive the same error message
        // with different error codes
        switch (Strings.nullToEmpty(errorResponse.getErrorMessage())) {
            case MUST_REGISTER_MESSAGE:
            case NO_ACTIVE_APPS_MESSAGE:
                throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(internalMessage);
            case ACCESS_TO_BANK_BLOCKED:
            case USER_ID_BLOCKED_MESSAGE:
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(internalMessage);
            case INCORRECT_CREDENTIALS_MESSAGE:
                throw LoginError.INCORRECT_CREDENTIALS.exception(internalMessage);
            case SIDSTE_CHANCE:
                throw LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT.exception(internalMessage);
            case COULD_NOT_RETRIEVE_MESSAGE:
                throw BankServiceError.BANK_SIDE_FAILURE.exception(internalMessage);
            default:
                return httpResponse;
        }
    }
}
