package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator;

import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants.ErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants.Scopes;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities.FailuresItem;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.GetCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class NordeaSeBankIdAuthenticator implements BankIdAuthenticator<AuthorizeResponse> {
    private final NordeaSeApiClient apiClient;
    private static final Logger log = LoggerFactory.getLogger(NordeaSeBankIdAuthenticator.class);
    private final String language;
    private final List<String> scopes;
    private OAuth2Token oAuth2Token;
    private String startTime;

    public NordeaSeBankIdAuthenticator(
            NordeaSeApiClient apiClient, String language, List<String> scopes) {
        this.apiClient = apiClient;
        this.language = language;
        this.scopes = scopes;
    }

    @Override
    public AuthorizeResponse init(String ssn)
            throws BankServiceException, LoginException, BankIdException {
        AuthorizeRequest authorizeRequest = getAuthorizeRequest(ssn);
        if (Strings.isNullOrEmpty(ssn)) {
            log.error("SSN was passed as empty or null!");
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        try {
            AuthorizeResponse authorizeResponse = apiClient.authorize(authorizeRequest);
            startTime = authorizeResponse.getGroupHeader().getCreationDateTime();
            return authorizeResponse;
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();

            if (response.getStatus() == HttpStatus.SC_BAD_REQUEST) {
                handleKnownAuthorizationErrors(response.getBody(ErrorResponse.class));
            }

            throw e;
        }
    }

    private void handleKnownAuthorizationErrors(ErrorResponse errorResponse)
            throws LoginException, BankIdException {

        if (errorResponse == null) {
            return;
        }

        if (errorResponse.isSsnInvalidError()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        // Will throw bankID exception if the error is related to know bankID errors. Logs
        // error code otherwise.
        getBankIdErrorStatus(errorResponse);
    }

    @Override
    public BankIdStatus collect(AuthorizeResponse reference)
            throws BankIdException, LoginException {
        try {
            HttpResponse response =
                    apiClient.getCode(
                            reference.getResponse().getOrderRef(),
                            reference.getResponse().getTppToken());
            if (response.getStatus() == HttpStatusCodes.STATUS_CODE_NOT_MODIFIED) {
                return BankIdStatus.WAITING;
            } else {
                return handleBankIdDone(response, reference);
            }
        } catch (HttpResponseException e) {
            ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
            if ((errorResponse.getError().getFailures().stream()
                            .findFirst()
                            .get()
                            .getDescription()
                            .equalsIgnoreCase(ErrorMessage.TIME_OUT_ERROR_NON_CUSTOMER))
                    || errorResponse.getError().getFailures().stream()
                                    .findFirst()
                                    .get()
                                    .getCode()
                                    .equalsIgnoreCase(
                                            ErrorMessage.NON_CUSTOMER_OR_CUSTOMER_TIME_OUT_ERROR)
                            && !checkBankIdTimeOutFromBankSide(
                                    startTime,
                                    errorResponse.getGroupHeader().getCreationDateTime())) {

                throw LoginError.NOT_CUSTOMER.exception();
            }
            return getBankIdErrorStatus(errorResponse);
        }
    }

    private boolean checkBankIdTimeOutFromBankSide(String startTime, String endTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-DD'T'HH:mm:ss");
        try {
            long diff = format.parse(endTime).getTime() - format.parse(startTime).getTime();
            if (diff >= NordeaSeConstants.TimeLimit.NON_CUSTOMER_BANKID) {
                return true;
            }
        } catch (ParseException e) {
            throw new IllegalStateException("Could not parse date");
        }
        return false;
    }

    private BankIdStatus handleBankIdDone(HttpResponse response, AuthorizeResponse reference) {
        GetCodeResponse getCodeResponse = response.getBody(GetCodeResponse.class);

        GetTokenForm form = getGetTokenForm(getCodeResponse);

        oAuth2Token = apiClient.getToken(form, reference.getResponse().getTppToken());

        return BankIdStatus.DONE;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.ofNullable(oAuth2Token);
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        try {
            return Optional.ofNullable(apiClient.refreshToken(refreshToken));
        } catch (HttpResponseException e) {
            // If these conditions are true, this will result in a session expired.
            if (e.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
                ErrorResponse error = e.getResponse().getBody(ErrorResponse.class);
                if (error.isInvalidRefreshTokenError()) {
                    log.warn("Nordea invalid refresh token spotted.");
                    return Optional.empty();
                }
            }
            throw e;
        }
    }

    private GetTokenForm getGetTokenForm(GetCodeResponse getCodeResponse) {
        return GetTokenForm.builder()
                .setCode(getCodeResponse.getResponse().getCode())
                .setGrantType(NordeaBaseConstants.FormValues.AUTHORIZATION_CODE)
                .build();
    }

    private AuthorizeRequest getAuthorizeRequest(String ssn) {
        return new AuthorizeRequest(
                NordeaSeConstants.FormValues.DURATION_MINUTES,
                language,
                ssn,
                NordeaSeConstants.FormValues.RESPONSE_TYPE,
                getScopes(),
                NordeaSeConstants.FormValues.STATE);
    }

    /**
     * Considers the scopes we should send to the bank.
     *
     * @return List of scopes based on provider payload.
     */
    private List<String> getScopes() {

        if (scopes.stream().allMatch(scope -> Scopes.AIS.equalsIgnoreCase(scope))) {
            // Return only AIS scopes
            return Arrays.asList(
                    FormValues.ACCOUNTS_BALANCES,
                    FormValues.ACCOUNTS_BASIC,
                    FormValues.ACCOUNTS_DETAILS,
                    FormValues.ACCOUNTS_TRANSACTIONS);
        } else if (scopes.stream()
                .allMatch(
                        scope ->
                                Scopes.AIS.equalsIgnoreCase(scope)
                                        || Scopes.PIS.equalsIgnoreCase(scope))) {
            // Return AIS + PIS scopes
            return Arrays.asList(
                    FormValues.ACCOUNTS_BALANCES,
                    FormValues.ACCOUNTS_BASIC,
                    FormValues.ACCOUNTS_DETAILS,
                    FormValues.ACCOUNTS_TRANSACTIONS,
                    FormValues.PAYMENTS_MULTIPLE);
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "%s contain invalid scope(s), only support scopes AIS and PIS",
                            scopes.toString()));
        }
    }

    public BankIdStatus getBankIdErrorStatus(ErrorResponse errorResponse) throws BankIdException {
        Optional<FailuresItem> error = errorResponse.getFailure();
        if (error.isPresent()) {
            String errorCode = error.get().getCode();
            if (ErrorMessage.CANCEL_ERROR.equalsIgnoreCase(errorCode)
                    || ErrorMessage.CANCELLED_ERROR.equalsIgnoreCase(errorCode)) {
                return BankIdStatus.CANCELLED;
            } else if (ErrorMessage.TIME_OUT_ERROR.equalsIgnoreCase(errorCode)) {
                return BankIdStatus.TIMEOUT;
            } else if (ErrorMessage.BANK_ID_IN_PROGRESS.equalsIgnoreCase(errorCode)) {
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            } else {
                log.error("Unknown Error code: " + errorCode);
                return BankIdStatus.FAILED_UNKNOWN;
            }
        } else {
            return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}
