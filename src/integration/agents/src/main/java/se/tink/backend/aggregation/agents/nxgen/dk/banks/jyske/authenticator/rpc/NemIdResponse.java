package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Decryptor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Token;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdResponse {
    private String data;

    public String getData() {
        return data;
    }

    public <C> C decrypt(Token token, Class<C> clazz) {
        return new Decryptor(token).read(this, clazz);
    }
}
