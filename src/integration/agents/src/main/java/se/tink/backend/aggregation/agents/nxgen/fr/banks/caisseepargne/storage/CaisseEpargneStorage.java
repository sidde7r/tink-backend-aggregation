package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.storage;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.IdentificationRoutingResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.storage.BpceStorage;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CaisseEpargneStorage extends BpceStorage {

    private static final String ID_ROUTING_AUTH_TOKEN = "ID_ROUTING_AUTH_TOKEN";
    private static final String ID_ROUTING_RESPONSE = "ID_ROUTING_RESPONSE";
    private static final String FINAL_AUTH_RESPONSE = "FINAL_AUTH_RESPONSE";

    public CaisseEpargneStorage(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    public Optional<OAuth2Token> getIdRoutingOAuth2Token() {
        return persistentStorage.get(ID_ROUTING_AUTH_TOKEN, OAuth2Token.class);
    }

    public void storeIdRoutingOAuth2Token(OAuth2Token oAuth2Token) {
        persistentStorage.put(ID_ROUTING_AUTH_TOKEN, oAuth2Token);
    }

    public IdentificationRoutingResponse getIdRoutingResponse() {
        return getOrThrowException(ID_ROUTING_RESPONSE, IdentificationRoutingResponse.class);
    }

    public void storeIdRoutingResponse(
            IdentificationRoutingResponse identificationRoutingResponse) {
        persistentStorage.put(ID_ROUTING_RESPONSE, identificationRoutingResponse);
    }

    public String getFinalAuthResponse() {
        return getOrThrowException(FINAL_AUTH_RESPONSE, String.class);
    }

    public void storeFinalAuthResponse(String finalAuthResponse) {
        persistentStorage.put(FINAL_AUTH_RESPONSE, finalAuthResponse);
    }
}
