package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

class StorageUtils {

    static String getTokenFromStorage(final PersistentStorage persistentStorage) {
        OAuth2Token oAuth2Token =
                persistentStorage
                        .get(CreditAgricoleBaseConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                CreditAgricoleBaseConstants.ErrorMessages
                                                        .UNABLE_LOAD_OAUTH_TOKEN));
        return oAuth2Token.getAccessToken();
    }
}
