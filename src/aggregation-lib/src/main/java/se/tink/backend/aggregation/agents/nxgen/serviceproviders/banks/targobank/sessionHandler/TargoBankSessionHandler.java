package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.sessionHandler;

import java.net.URISyntaxException;
import java.util.Optional;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.authentication.rpc.LogoutResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.sessionHandler.rpc.InitResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils.TargoBankUtils;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class TargoBankSessionHandler implements SessionHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(TargoBankSessionHandler.class);
    private final TargoBankApiClient apiClient;
    private final SessionStorage sessionStorage;

    private TargoBankSessionHandler(TargoBankApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public static TargoBankSessionHandler create(TargoBankApiClient apiClient, SessionStorage sessionStorage) {
        return new TargoBankSessionHandler(apiClient, sessionStorage);
    }

    @Override
    public void logout() {
        LogoutResponse logout = apiClient.logout();
        if (!TargoBankUtils.isSuccess(logout.getReturnCode())) {
            LOGGER.error("Could not log out!");
        }
    }

    @Override
    public void keepAlive() throws SessionException {
        InitResponse response = apiClient.actionInit(buildInitRequestBody());
        Optional.ofNullable(response).filter(o -> TargoBankUtils.isSuccess(o.getReturnCode()))
                .orElseThrow(() -> SessionError.SESSION_EXPIRED.exception());
    }

    private String buildInitRequestBody() {
        URIBuilder uriBuilder = new URIBuilder();
        try {
            return uriBuilder
                    .addParameter(TargoBankConstants.RequestBodyValues.ACTION,
                            TargoBankConstants.RequestBodyValues.INIT)
                    .addParameter(TargoBankConstants.RequestBodyValues.MEDIA,
                            TargoBankConstants.RequestBodyValues.MEDIA_VALUE)
                    .build().getQuery();
        } catch (URISyntaxException e) {
            LOGGER.error("Error building init request body\n", e);
            throw new RuntimeException(e);
        }
    }
}
