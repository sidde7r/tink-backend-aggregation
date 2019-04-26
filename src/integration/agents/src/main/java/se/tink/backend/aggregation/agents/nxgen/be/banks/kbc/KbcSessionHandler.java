package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.filters.KbcHttpFilter;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class KbcSessionHandler implements SessionHandler {
    private final KbcHttpFilter httpFilter;
    private final KbcApiClient apiClient;
    private final SessionStorage sessionStorage;

    KbcSessionHandler(
            KbcHttpFilter httpFilter, KbcApiClient apiClient, final SessionStorage sessionStorage) {
        this.httpFilter = httpFilter;
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        // Make sure set the Token to null before we try to login again
        httpFilter.resetHttpFilter();
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            final byte[] cipherKey =
                    EncodingUtils.decodeBase64String(
                            sessionStorage.get(KbcConstants.Encryption.AES_SESSION_KEY_KEY));

            apiClient.fetchAccounts(KbcConstants.DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS, cipherKey);
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
