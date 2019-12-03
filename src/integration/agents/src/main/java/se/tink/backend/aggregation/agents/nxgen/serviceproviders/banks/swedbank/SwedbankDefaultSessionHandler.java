package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants.LinkMethod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.TouchResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class SwedbankDefaultSessionHandler implements SessionHandler {
    private static final Logger log = LoggerFactory.getLogger(SwedbankDefaultSessionHandler.class);

    private final SwedbankDefaultApiClient apiClient;
    private final boolean checkKeepAlive;

    public SwedbankDefaultSessionHandler(
            SwedbankDefaultApiClient apiClient, boolean checkKeepAlive) {
        this.apiClient = apiClient;
        this.checkKeepAlive = checkKeepAlive;
    }

    @Override
    public void logout() {
        if (!apiClient.logout()) {
            log.warn("Logout failed");
        }
    }

    @Override
    public void keepAlive() throws SessionException {
        if (checkKeepAlive) {
            try {
                TouchResponse response = apiClient.touch();
                if (response != null
                        && !Strings.isNullOrEmpty(response.getBankId())
                        && !Strings.isNullOrEmpty(response.getChosenProfile())) {
                    completeAuthentication();
                    return;
                }
            } catch (Exception ex) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        }

        throw SessionError.SESSION_EXPIRED.exception();
    }

    private void completeAuthentication() throws AuthenticationException {
        apiClient.completeAuthentication(
                new LinkEntity()
                        .setMethod(LinkMethod.GET.getVerb())
                        .setUri(Url.PROFILE.get().get()));
    }
}
