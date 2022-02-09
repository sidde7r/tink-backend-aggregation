package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.consent.suppliers.ItemsSupplier;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.consent.SwedbankConsentHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.RefreshableItem;

@RequiredArgsConstructor
public class SwedbankDecoupledAuthenticator implements BankIdAuthenticator<String> {
    private final SwedbankApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final PersistentStorage persistentStorage;
    private final SwedbankConsentHandler consentHandler;
    private final AgentComponentProvider componentProvider;
    private String ssn;
    private String autoStartToken;
    private OAuth2Token accessToken;
    boolean shouldUseQRCodeOnAnotherDevice;

    @Override
    public String init(String ssn) {
        this.ssn = ssn;
        shouldUseQRCodeOnAnotherDevice = false;

        try {
            AuthenticationResponse authenticationResponse =
                    apiClient.authenticateDecoupled(ssn, shouldUseQRCodeOnAnotherDevice);

            if (shouldUseQRCodeOnAnotherDevice) {
                this.autoStartToken =
                        apiClient.fetchQRCodeImage(authenticationResponse.getAuthorizeId());
            } else {
                this.autoStartToken =
                        Optional.ofNullable(authenticationResponse.getChallengeData())
                                .map(ChallengeDataEntity::getAutoStartToken)
                                // Missing AST: Known defect on Swedbank side
                                .orElseThrow(BankIdError.UNKNOWN::exception);
            }
            return authenticationResponse.getAuthorizeId();
        } catch (HttpResponseException e) {
            handleBankIdError(e.getResponse().getBody(GenericResponse.class));
            throw e;
        }
    }

    @Override
    public BankIdStatus collect(String authorizeId) throws AuthenticationException {
        AuthenticationStatusResponse authenticationStatusResponse;

        try {
            authenticationStatusResponse = apiClient.collectAuthStatus(ssn, authorizeId);
        } catch (HttpResponseException e) {
            GenericResponse errorResponse = e.getResponse().getBody(GenericResponse.class);
            if (errorResponse.isMissingBankId()) {
                authenticationStatusResponse = handleMultipleEngagements(authorizeId);
            } else {
                handleBankIdError(errorResponse);
                throw e;
            }
        }

        if (authenticationStatusResponse.loginCanceled()) {
            return BankIdStatus.CANCELLED;
        }

        switch (Strings.nullToEmpty(authenticationStatusResponse.getScaStatus()).toLowerCase()) {
            case AuthStatus.RECEIVED:
            case AuthStatus.STARTED:
                if (shouldUseQRCodeOnAnotherDevice) {
                    this.autoStartToken = apiClient.fetchQRCodeImage(authorizeId);
                }
                return BankIdStatus.WAITING;
            case AuthStatus.EMPTY:
                return BankIdStatus.WAITING;
            case AuthStatus.FINALIZED:
                accessToken =
                        apiClient.exchangeCodeForToken(
                                authenticationStatusResponse.getAuthorizationCode());

                // Setup consents and validate no cross login
                completeAuthentication();

                return BankIdStatus.DONE;
            case AuthStatus.FAILED:
                return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    private void completeAuthentication() {
        persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, accessToken);

        consentHandler.getAndStoreConsentForAllAccounts();
        consentHandler.getListOfAccounts();
        consentHandler.getAndStoreDetailedConsent();

        if (ItemsSupplier.get(componentProvider.getCredentialsRequest())
                .contains(RefreshableItem.CHECKING_TRANSACTIONS)) {
            consentHandler.getAndStoreConsentForTransactionsOver90Days();
        }

        // Handle the case where the user has single engagement at Swedbank and selects
        // Savingsbank provider by mistake
        if (!apiClient.isSwedbank() && hasSwedbankAccounts()) {
            throw LoginError.NOT_CUSTOMER.exception(
                    SwedbankConstants.EndUserMessage.WRONG_BANK_SAVINGSBANK.getKey());
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
            throwNotCustomerException();
        }
        if (errorResponse.hasEmptyUserId() || errorResponse.hasWrongUserId()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        if (errorResponse.hasAuthenticationExpired()) {
            throw BankIdError.TIMEOUT.exception();
        }
        if (errorResponse.isAuthorizationError()) {
            throw AuthorizationError.UNAUTHORIZED.exception();
        }
    }

    private void throwNotCustomerException() {
        if (apiClient.isSwedbank()) {
            // This should be somehow extended - there are more than one savingsbank,
            // but we do not have possibility to check what savings bank it is.
            // Currently we are supporting only Swedbank through Decoupled flow.
            throw LoginError.NOT_CUSTOMER.exception(EndUserMessage.WRONG_BANK_SWEDBANK.getKey());
        } else {
            throw LoginError.NOT_CUSTOMER.exception(EndUserMessage.WRONG_BANK_SAVINGSBANK.getKey());
        }
    }

    private boolean hasSwedbankAccounts() {
        List<AccountEntity> accountList = apiClient.fetchAccounts().getAccounts();

        if (accountList.isEmpty()) {
            return false;
        }

        return SwedbankConstants.BANK_IDS.get(0).equals(accountList.get(0).getBankId().trim());
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

        persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, token);

        consentHandler.verifyValidConsentOrThrow();

        return Optional.ofNullable(token);
    }
}
