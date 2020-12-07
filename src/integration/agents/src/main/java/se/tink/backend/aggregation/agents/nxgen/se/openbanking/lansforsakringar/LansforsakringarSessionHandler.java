package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@RequiredArgsConstructor
@Slf4j
public class LansforsakringarSessionHandler implements SessionHandler {

    private final LansforsakringarApiClient apiClient;
    private final LansforsakringarStorageHelper storageHelper;

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        OAuth2Token token = fetchTokenFromPermanentStorage();

        if (token.hasAccessExpired()) {
            refreshAndStoreNewToken(token);
            return;
        }

        try {
            String resourceId = getSampleAccountIdFromStorage();

            // We fetch first account balance and store it, then when actual balance fetching occurs
            // we retrieve balance for first account from storage and remove it. This logic helps us
            // to limit balance fetching request and in result increase the number of background
            // refreshes
            GetBalancesResponse balanceResponse = apiClient.getBalances(resourceId);
            storageHelper.storeBalanceResponse(resourceId, balanceResponse);
            return;
        } catch (BankServiceException e) {
            log.warn("Error when fetching transactions for account - in session handler", e);
        }
        clearDataFromStorage();
        throw SessionError.SESSION_EXPIRED.exception();
    }

    private OAuth2Token fetchTokenFromPermanentStorage() throws SessionException {
        return storageHelper.getOAuth2Token().orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    private void refreshAndStoreNewToken(OAuth2Token token) throws SessionException {
        OAuth2Token refreshToken =
                apiClient.refreshToken(
                        token.getRefreshToken()
                                .orElseThrow(SessionError.SESSION_EXPIRED::exception));
        storageHelper.setOAuth2Token(refreshToken);
    }

    /*
       Need to check transactions not accounts - we cannot make a call for accounts without PSU.
       Sample account is obtained from persistent storage.
    */
    private String getSampleAccountIdFromStorage() {
        return storageHelper.getStoredAccounts().map(GetAccountsResponse::getAccounts)
                .orElseGet(Collections::emptyList).stream()
                .findFirst()
                .map(AccountEntity::getResourceId)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    /*
     * With lansforsakringar we can refresh the token but have a expired consentId which will cause the agent to think we have a valid session.
     * Therefore, all storage is cleared.
     */
    private void clearDataFromStorage() throws SessionException {
        storageHelper.clearSessionData();
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
