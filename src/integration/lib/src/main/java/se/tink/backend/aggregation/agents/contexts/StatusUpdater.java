package se.tink.backend.aggregation.agents.contexts;

import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.libraries.i18n.Catalog;

public interface StatusUpdater {
    void updateStatus(CredentialsStatus status);

    void updateStatus(CredentialsStatus status, String statusPayload, boolean statusFromProvider);

    void updateStatusWithError(
            CredentialsStatus status, String statusPayload, ConnectivityError error);

    Catalog getCatalog();

    default void updateStatus(CredentialsStatus status, String statusPayload) {
        updateStatus(status, statusPayload, true);
    }
}
