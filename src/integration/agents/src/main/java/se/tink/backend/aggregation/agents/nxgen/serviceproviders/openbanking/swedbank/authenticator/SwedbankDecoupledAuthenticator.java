package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.AuthStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.SwedbankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class SwedbankDecoupledAuthenticator implements BankIdAuthenticator<String> {
    private final SwedbankApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SwedbankTransactionalAccountFetcher transactionalAccountFetcher;
    private final PersistentStorage persistentStorage;
    private String ssn;
    private String autoStartToken;
    private OAuth2Token accessToken;

    @Override
    public String init(String ssn) {
        this.ssn = ssn;
        try {
            AuthenticationResponse authenticationResponse = apiClient.authenticateDecoupled(ssn);
            this.autoStartToken =
                    Optional.ofNullable(authenticationResponse.getChallengeData())
                            .map(ChallengeDataEntity::getAutoStartToken)
                            // Missing AST: Known defect on Swedbank side
                            .orElseThrow(BankIdError.UNKNOWN::exception);
            return authenticationResponse.getCollectAuthUri();
        } catch (HttpResponseException e) {
            handleBankIdError(e.getResponse().getBody(GenericResponse.class));
            throw e;
        }
    }

    @Override
    public BankIdStatus collect(String collectAuthUri) throws AuthenticationException {
        AuthenticationStatusResponse authenticationStatusResponse;

        try {
            authenticationStatusResponse = apiClient.collectAuthStatus(ssn, collectAuthUri);
        } catch (HttpResponseException e) {
            GenericResponse errorResponse = e.getResponse().getBody(GenericResponse.class);
            if (errorResponse.isMissingBankId()) {
                authenticationStatusResponse = handleMultipleEngagements(collectAuthUri);
            } else {
                handleBankIdError(errorResponse);
                throw e;
            }
        }

        if (authenticationStatusResponse.loginCanceled()) {
            return BankIdStatus.CANCELLED;
        }

        switch (authenticationStatusResponse.getScaStatus().toLowerCase()) {
            case AuthStatus.RECEIVED:
            case AuthStatus.STARTED:
                return BankIdStatus.WAITING;
            case AuthStatus.FINALIZED:
                accessToken =
                        apiClient.exchangeCodeForToken(
                                authenticationStatusResponse.getAuthorizationCode());
                // Handle the case where the user has single engagement at Swedbank and selects
                // Savingsbank provider by mistake
                if (!apiClient.isSwedbank()) {
                    persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, accessToken);
                    if (transactionalAccountFetcher.isCrossLogin()) {
                        throw LoginError.NOT_CUSTOMER.exception(
                                SwedbankConstants.EndUserMessage.WRONG_BANK_SAVINGSBANK.getKey());
                    }
                }
                return BankIdStatus.DONE;
            case AuthStatus.FAILED:
                return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    private AuthenticationStatusResponse handleMultipleEngagements(String collectAuthUri) {
        if (apiClient.isSwedbank()) {
            return apiClient.supplyBankId(ssn, collectAuthUri, SwedbankConstants.BANK_IDS.get(0));
        } else {
            return apiClient.supplyBankId(
                    ssn,
                    collectAuthUri,
                    SwedbankConstants.BANK_IDS.get(
                            Integer.parseInt(supplementalInformationHelper.waitForLoginInput())));
        }
    }

    private void handleBankIdError(GenericResponse errorResponse) {

        if (errorResponse.isLoginInterrupted()) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception();
        }
        if (errorResponse.isKycError()) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                    EndUserMessage.MUST_UPDATE_AGREEMENT.getKey());
        }
        if (errorResponse.isMissingBankAgreement()) {
            throw LoginError.NOT_CUSTOMER.exception();
        }
        if (errorResponse.isNoProfileAvailable()) {
            if (apiClient.isSwedbank()) {
                // This should be somehow extended - there are more than one savingsbank,
                // but we do not have possibility to check what savings bank it is.
                // Currently we are supporting only Swedbank through Decoupled flow.
                throw LoginError.NOT_CUSTOMER.exception(
                        SwedbankConstants.EndUserMessage.WRONG_BANK_SAVINGSBANK.getKey());
            } else {
                throw LoginError.NOT_CUSTOMER.exception(
                        SwedbankConstants.EndUserMessage.WRONG_BANK_SWEDBANK.getKey());
            }
        }
        if (errorResponse.hasEmptyUserId() || errorResponse.hasWrongUserId()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        if (errorResponse.hasAuthenticationExpired()) {
            throw BankIdError.TIMEOUT.exception();
        }
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autoStartToken);
    }

    @Override
    public String refreshAutostartToken()
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        return init(ssn);
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.ofNullable(accessToken);
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) throws SessionException {
        OAuth2Token token;
        try {
            token = apiClient.refreshToken(refreshToken);
        } catch (HttpResponseException e) {
            GenericResponse response = e.getResponse().getBody(GenericResponse.class);
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST
                    && response.refreshTokenHasExpired()) {
                throw SessionError.SESSION_EXPIRED.exception(e);
            }
            throw e;
        }
        return Optional.ofNullable(token);
    }
}
