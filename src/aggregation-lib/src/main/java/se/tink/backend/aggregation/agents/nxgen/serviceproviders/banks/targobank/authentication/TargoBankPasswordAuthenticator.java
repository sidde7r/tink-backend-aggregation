package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.authentication;

import java.net.URISyntaxException;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils.TargoBankUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class TargoBankPasswordAuthenticator implements PasswordAuthenticator {

    private final Logger LOGGER = LoggerFactory.getLogger(TargoBankPasswordAuthenticator.class);
    private final TargoBankApiClient apiClient;

    private TargoBankPasswordAuthenticator(TargoBankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static TargoBankPasswordAuthenticator create(TargoBankApiClient apiClient) {
        return new TargoBankPasswordAuthenticator(apiClient);
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {
        LoginResponse logon = apiClient.logon(buildBodyLogonRequest(username, password));
        if (!TargoBankUtils.isSuccess(logon.getReturnCode())) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private String buildBodyLogonRequest(String username, String password) {
        URIBuilder uriBuilder = new URIBuilder();
        try {
            return uriBuilder.addParameter(TargoBankConstants.RequestBodyValues.USER, username)
                    .addParameter(TargoBankConstants.RequestBodyValues.PASSWORD, password)
                    .addParameter(TargoBankConstants.RequestBodyValues.APP_VERSION,
                            TargoBankConstants.RequestBodyValues.APP_VERSION_VALUE)
                    .addParameter(TargoBankConstants.RequestBodyValues.CIBLE,
                            TargoBankConstants.RequestBodyValues.CIBLE_VALUE)
                    .addParameter(TargoBankConstants.RequestBodyValues.WS_VERSION,
                            "2")
                    .addParameter(TargoBankConstants.RequestBodyValues.MEDIA,
                            TargoBankConstants.RequestBodyValues.MEDIA_VALUE)
                    .build().getQuery();
        } catch (URISyntaxException e) {
            LOGGER.error("Error building login body request\n", e);
            throw new RuntimeException(e);
        }
    }
}
