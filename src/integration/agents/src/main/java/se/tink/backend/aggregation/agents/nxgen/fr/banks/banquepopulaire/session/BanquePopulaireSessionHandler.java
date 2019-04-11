package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.AppConfigEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.BankEntity;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BanquePopulaireSessionHandler implements SessionHandler {

    private final BanquePopulaireApiClient apiClient;
    private final SessionStorage sessionStorage;

    public BanquePopulaireSessionHandler(
            BanquePopulaireApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        sessionStorage
                .get(BanquePopulaireConstants.Storage.BANK_ENTITY, BankEntity.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
        sessionStorage
                .get(BanquePopulaireConstants.Storage.APP_CONFIGURATION, AppConfigEntity.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        try {
            if (!apiClient.keepAlive()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
