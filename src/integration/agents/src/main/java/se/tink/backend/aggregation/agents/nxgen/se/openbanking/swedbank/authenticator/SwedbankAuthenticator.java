package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.AuthenticationStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SwedbankAuthenticator implements OAuth2Authenticator {
    private final SwedbankApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public SwedbankAuthenticator(SwedbankApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return apiClient.exchangeCodeForToken(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
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
        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, accessToken);
    }

    public AuthenticationResponse init(String ssn) {
        try {
            return apiClient.authenticate(ssn);
        } catch (HttpResponseException e) {
            GenericResponse response = e.getResponse().getBody(GenericResponse.class);
            if (response.hasEmptyUserId() || response.hasWrongUserId()) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
            throw e;
        }
    }

    public AuthenticationStatusResponse collect(String ssn, String collectAuthUri) {
        return apiClient.collectAuthStatus(ssn, collectAuthUri);
    }

    public void useConsent(ConsentResponse consentResponse) {
        persistentStorage.put(
                SwedbankConstants.StorageKeys.CONSENT, consentResponse.getConsentId());
    }

    public Optional<ConsentResponse> getConsentForIbanList() {
        FetchAccountResponse accountsResponse;
        try {
            accountsResponse = apiClient.fetchAccounts();
        } catch (HttpResponseException e) {
            if (e.getResponse().getBody(GenericResponse.class).isKycError()) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                        EndUserMessage.MUST_ANSWER_KYC.getKey());
            }
            throw e;
        }

        return accountsResponse.getAccountList().isEmpty()
                ? Optional.empty()
                : Optional.of(
                        apiClient.getConsentAccountDetails(
                                mapAccountResponseToIbanList(accountsResponse)));
    }

    public ConsentResponse getConsentForAllAccounts() {
        return apiClient.getConsentAllAccounts();
    }

    public boolean getConsentStatus(String consentId) {
        return apiClient.checkIfConsentIsApproved(consentId);
    }

    public String getScaStatus(String statusLink) {
        return apiClient.getScaStatus(statusLink);
    }

    public AuthenticationResponse initiateAuthorization(String authorizationLink) {
        return apiClient.startAuthorization(authorizationLink);
    }

    private List<String> mapAccountResponseToIbanList(FetchAccountResponse accounts) {
        return accounts.getAccountList().stream()
                .map(AccountEntity::getIban)
                .collect(Collectors.toList());
    }
}
