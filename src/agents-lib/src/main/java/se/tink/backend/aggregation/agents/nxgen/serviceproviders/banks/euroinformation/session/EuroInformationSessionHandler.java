package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LogoutResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session.rpc.PfmInitResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class EuroInformationSessionHandler implements SessionHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(EuroInformationSessionHandler.class);
    private final EuroInformationApiClient apiClient;

    private EuroInformationSessionHandler(EuroInformationApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static EuroInformationSessionHandler create(EuroInformationApiClient apiClient) {
        return new EuroInformationSessionHandler(apiClient);
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
        PfmInitResponse response = apiClient.actionInit();
        Optional.ofNullable(response).filter(o -> EuroInformationUtils.isSuccess(o.getReturnCode()))
                .orElseThrow(() -> SessionError.SESSION_EXPIRED.exception());
    }
}
