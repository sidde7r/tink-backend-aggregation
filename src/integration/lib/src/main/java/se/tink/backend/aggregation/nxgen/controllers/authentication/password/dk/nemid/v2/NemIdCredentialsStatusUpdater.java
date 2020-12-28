package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.libraries.i18n.Catalog;

@Slf4j
@RequiredArgsConstructor
public class NemIdCredentialsStatusUpdater {

    private final StatusUpdater statusUpdater;
    private final Catalog catalog;

    public void updateStatusPayload(Credentials credentials, UserMessage userMessage) {
        String message = catalog.getString(userMessage);
        log.info(
                "Updating payload: {} (current credential status: {})",
                message,
                credentials.getStatus());
        statusUpdater.updateStatus(credentials.getStatus(), message);
    }
}
