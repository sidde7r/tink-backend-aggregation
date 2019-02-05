package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionRequest {

    @JsonProperty("Usuario")
    private String user;

    @JsonProperty("Clave")
    private String password;

    @JsonProperty("Tarjeta")
    private boolean card;

    @JsonProperty("UltimoAcceso")
    private String lastAccess;

    public SessionRequest(String user, String password, boolean card, String lastAccess) {
        this.user = user;
        this.password = password;
        this.card = card;
        this.lastAccess = lastAccess;
    }
}
