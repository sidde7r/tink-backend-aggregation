package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LogoutResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session.rpc.PfmInitResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class EuroInformationSessionHandler implements SessionHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(EuroInformationSessionHandler.class);
    private final EuroInformationApiClient apiClient;
    private final EuroInformationConfiguration config;

    private EuroInformationSessionHandler(
            EuroInformationApiClient apiClient, EuroInformationConfiguration config) {
        this.apiClient = apiClient;
        this.config = config;
    }

    public static EuroInformationSessionHandler create(
            EuroInformationApiClient apiClient, EuroInformationConfiguration config) {
        return new EuroInformationSessionHandler(apiClient, config);
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
        Optional<String> returnCode =
                config.getInitEndpoint()
                        .map(
                                endpoint -> {
                                    PfmInitResponse response =
                                            apiClient.actionInit(EuroInformationConstants.Url.INIT);
                                    return Optional.ofNullable(response)
                                            .map(m -> m.getReturnCode());
                                })
                        .orElseGet(
                                () -> {
                                    AccountSummaryResponse accountSummaryResponse =
                                            apiClient.requestAccounts();
                                    return Optional.ofNullable(accountSummaryResponse)
                                            .map(m -> m.getReturnCode());
                                });

        returnCode
                .filter(EuroInformationUtils::isSuccess)
                .orElseThrow(() -> SessionError.SESSION_EXPIRED.exception());
    }
}
