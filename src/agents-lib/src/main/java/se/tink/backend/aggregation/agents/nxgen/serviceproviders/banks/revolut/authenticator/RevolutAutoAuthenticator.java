package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator;

import com.google.api.client.http.HttpStatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class RevolutAutoAuthenticator implements AutoAuthenticator {
    private static final Logger log = LoggerFactory.getLogger(RevolutAutoAuthenticator.class);
    private final RevolutApiClient apiClient;

    public RevolutAutoAuthenticator(RevolutApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        try {
            apiClient.fetchUser();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            log.error("%s: Authorization failed with message \"%s\"",
                    RevolutConstants.Tags.AUTHORIZATION_ERROR,
                    e.getResponse().getBody(ErrorResponse.class).getMessage());

            throw e;
        }
    }
}
