package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class CreditAgricoleStorage {

    private final PersistentStorage persistentStorage;

    public String getTokenFromStorage() {
        final OAuth2Token oAuth2Token =
                persistentStorage
                        .get(CreditAgricoleBaseConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                CreditAgricoleBaseConstants.ErrorMessages
                                                        .UNABLE_LOAD_OAUTH_TOKEN));
        return oAuth2Token.getAccessToken();
    }

    public void storeToken(OAuth2Token token) {
        persistentStorage.put(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, token);
    }

    public void storeInitialFetchState(Boolean isInitialFetch) {
        persistentStorage.put(
                CreditAgricoleBaseConstants.StorageKeys.IS_INITIAL_FETCH, isInitialFetch);
    }
}
