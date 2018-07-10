package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.sessionHandler;

import java.net.URISyntaxException;
import java.util.Optional;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LogoutResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.sessionHandler.rpc.InitResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class EuroInformationSessionHandler implements SessionHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(EuroInformationSessionHandler.class);
    private final EuroInformationApiClient apiClient;
    private final SessionStorage sessionStorage;

    private EuroInformationSessionHandler(EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public static EuroInformationSessionHandler create(EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        return new EuroInformationSessionHandler(apiClient, sessionStorage);
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
        InitResponse response = apiClient.actionInit(buildInitRequestBody());
        Optional.ofNullable(response).filter(o -> EuroInformationUtils.isSuccess(o.getReturnCode()))
                .orElseThrow(() -> SessionError.SESSION_EXPIRED.exception());
    }

    private String buildInitRequestBody() {
        URIBuilder uriBuilder = new URIBuilder();
        try {
            return uriBuilder
                    .addParameter(EuroInformationConstants.RequestBodyValues.ACTION,
                            EuroInformationConstants.RequestBodyValues.INIT)
                    .addParameter(EuroInformationConstants.RequestBodyValues.MEDIA,
                            EuroInformationConstants.RequestBodyValues.MEDIA_VALUE)
                    .build().getQuery();
        } catch (URISyntaxException e) {
            LOGGER.error("Error building init request body\n", e);
            throw new RuntimeException(e);
        }
    }
}
