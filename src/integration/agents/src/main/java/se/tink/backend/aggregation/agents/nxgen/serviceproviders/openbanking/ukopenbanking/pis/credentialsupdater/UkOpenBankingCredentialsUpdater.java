package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.credentialsupdater;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;

@RequiredArgsConstructor
public class UkOpenBankingCredentialsUpdater {

    private final Credentials credentials;

    private final SystemUpdater systemUpdater;

    public void updateCredentialsStatus(CredentialsStatus status) {
        credentials.setSupplementalInformation(null);
        credentials.setStatusPayload(null);
        credentials.setStatus(status);

        systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, true);
    }
}
