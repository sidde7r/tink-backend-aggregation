package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v21.authenticator;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v21.NordeaFiApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.authenticator.NordeaV21Authenticator;

public class NordeaFiAuthenticator extends NordeaV21Authenticator<NordeaFiApiClient> {

    public NordeaFiAuthenticator(NordeaFiApiClient client) {
        super(client);
    }
}
