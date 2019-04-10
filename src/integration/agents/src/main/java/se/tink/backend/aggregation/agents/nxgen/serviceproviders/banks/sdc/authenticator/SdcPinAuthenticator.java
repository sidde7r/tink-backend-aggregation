package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MultivaluedMap;
import org.eclipse.jetty.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreements;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.AgreementsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.InvalidPinResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SelectAgreementRequest;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class SdcPinAuthenticator implements PasswordAuthenticator {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(SdcPinAuthenticator.class);

    private final SdcApiClient bankClient;
    private final SdcSessionStorage sessionStorage;
    private final SdcConfiguration agentConfiguration;

    public SdcPinAuthenticator(
            SdcApiClient bankClient,
            SdcSessionStorage sessionStorage,
            SdcConfiguration agentConfiguration) {
        this.bankClient = bankClient;
        this.sessionStorage = sessionStorage;
        this.agentConfiguration = agentConfiguration;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {

        try {
            AgreementsResponse agreementsResponse = bankClient.pinLogon(username, password);

            if (agreementsResponse.isEmpty()) {
                LOGGER.warnExtraLong(
                        "User was able to login, but has no agreements?",
                        SdcConstants.Session.LOGIN);
            }
            sessionStorage.setAgreements(agreementsResponse.toSessionStorageAgreements());
            if (tokenNeeded(sessionStorage.getAgreements())) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (HttpResponseException e) {
            // sdc responds with internal server error when bad credentials

            Optional<InvalidPinResponse> invalidPin = InvalidPinResponse.from(e);
            if (invalidPin.isPresent()) {
                throw invalidPin.get().exception();
            }
            if (isInternalError(e)) {
                // errorMessage is null safe
                String errorMessage =
                        Optional.ofNullable(
                                        e.getResponse()
                                                .getHeaders()
                                                .getFirst(SdcConstants.Headers.X_SDC_ERROR_MESSAGE))
                                .orElse("");
                if (this.agentConfiguration.isNotCustomer(errorMessage)) {
                    throw LoginError.NOT_CUSTOMER.exception();
                } else if (agentConfiguration.isLoginError(errorMessage)) {
                    LOGGER.info(errorMessage);

                    // if user is blocked throw more specific exception
                    if (agentConfiguration.isUserBlocked(errorMessage)) {
                        throw AuthorizationError.ACCOUNT_BLOCKED.exception();
                    }

                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                }
            }

            throw e;
        }
    }

    private boolean isInternalError(Exception e) {
        if (e instanceof HttpResponseException) {
            HttpResponse response = ((HttpResponseException) e).getResponse();
            int statusCode = response.getStatus();
            return statusCode == HttpStatus.INTERNAL_SERVER_ERROR_500;
        }

        return false;
    }

    /*
       Check if device token is needed by backend or if session is expired. Called during logon.
    */
    private boolean tokenNeeded(SessionStorageAgreements agreements) throws SessionException {
        SessionStorageAgreement agreement =
                agreements.stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No agreement found"));

        HttpResponse response =
                bankClient.internalSelectAgreement(
                        new SelectAgreementRequest()
                                .setUserNumber(agreement.getUserNumber())
                                .setAgreementNumber(agreement.getAgreementId()));

        return tokenNeeded(response);
    }

    // header field action code indicates if we need to create/renew our device token
    private boolean tokenNeeded(HttpResponse response) {
        return hasActionCode(response, SdcConstants.Headers.ACTION_CODE_TOKEN_NEEDED);
    }

    // header field action code has any of the specified values
    private boolean hasActionCode(HttpResponse response, List<String> actionCodeValues) {
        MultivaluedMap<String, String> headers = response.getHeaders();
        return headers.containsKey(SdcConstants.Headers.X_SDC_ACTION_CODE)
                && headers.get(SdcConstants.Headers.X_SDC_ACTION_CODE).stream()
                        .filter(headerValue -> (!Strings.isNullOrEmpty(headerValue)))
                        .map(String::toUpperCase)
                        .anyMatch(headerValue -> (actionCodeValues.contains(headerValue)));
    }
}
