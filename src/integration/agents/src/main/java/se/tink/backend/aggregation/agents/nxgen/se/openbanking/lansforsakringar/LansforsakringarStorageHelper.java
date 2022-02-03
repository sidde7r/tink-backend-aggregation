package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountNumbersResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class LansforsakringarStorageHelper {

    private final PersistentStorage persistentStorage;

    public void setConsentId(String consentId) {
        persistentStorage.put(LansforsakringarConstants.StorageKeys.CONSENT_ID, consentId);
    }

    public void setAuthorisationId(String authorisationId) {
        persistentStorage.put(StorageKeys.AUTHORISATION_ID, authorisationId);
    }

    public void setOAuth2Token(OAuth2Token refreshToken) {
        persistentStorage.put(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, refreshToken);
    }

    public void storeAccounts(GetAccountsResponse accountResponse) {
        persistentStorage.put(LansforsakringarConstants.StorageKeys.ACCOUNTS, accountResponse);
    }

    public void storeBalanceResponse(String resourceId, GetBalancesResponse balanceResponse) {
        persistentStorage.put(resourceId, balanceResponse);
    }

    public void storeAccountNumbers(AccountNumbersResponse accountNumbersResponse) {
        persistentStorage.put(
                LansforsakringarConstants.StorageKeys.ACCOUNT_NUMBERS, accountNumbersResponse);
    }

    public String getConsentId() {
        return persistentStorage.get(LansforsakringarConstants.StorageKeys.CONSENT_ID);
    }

    public String getAuthorisationId() {
        return persistentStorage.get(StorageKeys.AUTHORISATION_ID);
    }

    public Optional<OAuth2Token> getOAuth2Token() {
        return persistentStorage.get(
                OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class);
    }

    public Optional<GetAccountsResponse> getStoredAccounts() {
        return persistentStorage.get(
                LansforsakringarConstants.StorageKeys.ACCOUNTS, GetAccountsResponse.class);
    }

    public Optional<GetBalancesResponse> getStoredBalanceResponse(String resourceId) {
        return persistentStorage.get(resourceId, GetBalancesResponse.class);
    }

    public Optional<AccountNumbersResponse> getStoredAccountNumbers() {
        return persistentStorage.get(
                LansforsakringarConstants.StorageKeys.ACCOUNT_NUMBERS,
                AccountNumbersResponse.class);
    }

    public void removeBalanceResponseFromStorage(String resourceId) {
        persistentStorage.remove(resourceId);
    }

    public void clearSessionData() {
        persistentStorage.remove(LansforsakringarConstants.StorageKeys.ACCOUNTS);
        persistentStorage.remove(LansforsakringarConstants.StorageKeys.CONSENT_ID);
        persistentStorage.remove(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN);
    }
}
