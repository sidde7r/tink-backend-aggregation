package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Encryptable;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Token;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdGenericRequest {

    private NemIdGenericRequest() {
    }

    private String data;

    public static NemIdGenericRequest create(Token token, Encryptable encryptable) {
        NemIdGenericRequest request = new NemIdGenericRequest();
        request.data = Encryptable.encrypt(token, encryptable);
        return request;
    }

}
