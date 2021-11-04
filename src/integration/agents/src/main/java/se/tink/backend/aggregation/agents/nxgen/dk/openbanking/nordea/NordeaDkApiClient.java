package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordea;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class NordeaDkApiClient extends NordeaBaseApiClient {

    public NordeaDkApiClient(
            AgentComponentProvider componentProvider,
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            QsealcSigner qsealcSigner,
            StrongAuthenticationState strongAuthenticationState) {
        super(
                componentProvider,
                client,
                persistentStorage,
                qsealcSigner,
                false,
                strongAuthenticationState);
    }
}
