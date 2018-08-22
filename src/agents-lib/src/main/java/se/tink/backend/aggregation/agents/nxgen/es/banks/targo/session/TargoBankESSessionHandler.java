package se.tink.backend.aggregation.agents.nxgen.es.banks.targo.session;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LogoutResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class TargoBankESSessionHandler implements SessionHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(TargoBankESSessionHandler.class);
    private final EuroInformationApiClient apiClient;
    private final SessionStorage sessionStorage;

    private TargoBankESSessionHandler(EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public static TargoBankESSessionHandler create(EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        return new TargoBankESSessionHandler(apiClient, sessionStorage);
    }

    @Override
    public void logout() {
        LogoutResponse logout = apiClient.logout();
        if (!EuroInformationUtils.isSuccess(logout.getReturnCode())) {
            LOGGER.error("Could not log out!");
        }
    }

    @Override
    public void keepAlive() throws SessionException {
        AccountSummaryResponse response = apiClient.requestAccounts();
        Optional.ofNullable(response).filter(o -> EuroInformationUtils.isSuccess(o.getReturnCode()))
                .orElseThrow(() -> SessionError.SESSION_EXPIRED.exception());
    }
}
