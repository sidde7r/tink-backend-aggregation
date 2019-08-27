package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator;

import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.Arrays;
import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.GetCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.RefreshTokenForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class NordeaSeBankIdAuthenticator implements BankIdAuthenticator<AuthorizeResponse> {
    private final NordeaSeApiClient apiClient;
    private static final Logger log = LoggerFactory.getLogger(NordeaSeBankIdAuthenticator.class);
    private final String language;

    public NordeaSeBankIdAuthenticator(NordeaSeApiClient apiClient, String language) {
        this.apiClient = apiClient;
        this.language = language;
    }

    @Override
    public AuthorizeResponse init(String ssn) throws BankServiceException, LoginException {
        AuthorizeRequest authorizeRequest = getAuthorizeRequest(ssn);
        if (Strings.isNullOrEmpty(ssn)) {
            log.error("SSN was passed as empty or null!");
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        try {
            return apiClient.authorize(authorizeRequest);
        } catch (HttpResponseException e) {
            if (isSsnInvalidError(e)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
            throw e;
        }
    }

    private boolean isSsnInvalidError(HttpResponseException e) {
        if (e.getResponse().getStatus() != HttpStatus.SC_BAD_REQUEST) {
            return false;
        }
        ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
        if (errorResponse == null) {
            return false;
        }

        return errorResponse.isSsnInvalidError();
    }

    @Override
    public BankIdStatus collect(AuthorizeResponse reference) {
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
            String exceptionBody = e.getResponse().getBody(String.class);
            log.error(
                    "{}, Authorization failed with message \"{}\"",
                    NordeaSeConstants.Tags.AUTHORIZATION_ERROR,
                    exceptionBody);
            if (exceptionBody.contains(NordeaSeConstants.ErrorMessage.CANCEL_ERROR)) {
                return BankIdStatus.CANCELLED;
            } else if (exceptionBody.contains(NordeaSeConstants.ErrorMessage.TIME_OUT_ERROR)) {
                return BankIdStatus.TIMEOUT;
            } else {
                return BankIdStatus.FAILED_UNKNOWN;
            }
        }
    }

    private BankIdStatus handleBankIdDone(HttpResponse response, AuthorizeResponse reference) {
        GetCodeResponse getCodeResponse = response.getBody(GetCodeResponse.class);

        GetTokenForm form = getGetTokenForm(getCodeResponse);

        OAuth2Token accessToken = apiClient.getToken(form, reference.getResponse().getTppToken());
        apiClient.storeToken(accessToken);

        return BankIdStatus.DONE;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.ofNullable(apiClient.getStoredToken());
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        RefreshTokenForm form =
                RefreshTokenForm.builder()
                        .setRefreshToken(refreshToken)
                        .setGrantType(NordeaBaseConstants.FormValues.REFRESH_TOKEN)
                        .setRedirectUri(apiClient.getConfiguration().getRedirectUrl())
                        .build();

        OAuth2Token token = apiClient.refreshToken(form);
        apiClient.storeToken(token);
        return Optional.ofNullable(token);
    }

    private GetTokenForm getGetTokenForm(GetCodeResponse getCodeResponse) {
        return GetTokenForm.builder()
                .setCode(getCodeResponse.getResponse().getCode())
                .setGrantType(NordeaBaseConstants.FormValues.AUTHORIZATION_CODE)
                .setRedirectUri(apiClient.getConfiguration().getRedirectUrl())
                .build();
    }

    private AuthorizeRequest getAuthorizeRequest(String ssn) {
        return new AuthorizeRequest(
                NordeaSeConstants.FormValues.DURATION_MINUTES,
                language,
                ssn,
                apiClient.getConfiguration().getRedirectUrl(),
                NordeaSeConstants.FormValues.RESPONSE_TYPE,
                Arrays.asList(
                        NordeaSeConstants.FormValues.ACCOUNTS_BALANCES,
                        NordeaSeConstants.FormValues.ACCOUNTS_BASIC,
                        NordeaSeConstants.FormValues.ACCOUNTS_DETAILS,
                        NordeaSeConstants.FormValues.ACCOUNTS_TRANSACTIONS,
                        NordeaSeConstants.FormValues.PAYMENTS_MULTIPLE),
                NordeaSeConstants.FormValues.STATE);
    }
}
