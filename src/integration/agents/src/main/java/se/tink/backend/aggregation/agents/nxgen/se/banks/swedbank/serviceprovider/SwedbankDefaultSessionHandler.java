package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.LinkMethod;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.TouchResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SwedbankDefaultSessionHandler implements SessionHandler {
    private static final Logger log = LoggerFactory.getLogger(SwedbankDefaultSessionHandler.class);

    private final SwedbankDefaultApiClient apiClient;

    public SwedbankDefaultSessionHandler(SwedbankDefaultApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        if (!apiClient.logout()) {
            log.warn("Logout failed");
        }
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            TouchResponse response = apiClient.touch();
            if (response != null
                    && !Strings.isNullOrEmpty(response.getBankId())
                    && !Strings.isNullOrEmpty(response.getChosenProfile())) {
                completeAuthentication();
                return;
            }
        } catch (HttpResponseException ex) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        throw SessionError.SESSION_EXPIRED.exception();
    }

    private void completeAuthentication() throws AuthenticationException {
        apiClient.completeAuthentication(
                new LinkEntity().setMethod(LinkMethod.GET.getVerb()).setUri(Url.PROFILE.getPath()));
    }
}
