package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.storage.BpceStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class BpceAuthenticator extends StatelessProgressiveAuthenticator {

    protected abstract BpceStorage getBpceStorage();

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return !getBpceStorage().getCouldAutoAuthenticate().orElse(false);
    }
}
