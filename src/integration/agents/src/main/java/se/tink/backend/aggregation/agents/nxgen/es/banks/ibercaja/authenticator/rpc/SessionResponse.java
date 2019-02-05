package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionResponse {

    @JsonProperty("NICI")
    private int nici;
    @JsonProperty("Ticket")
    private String ticket;
    @JsonProperty("CodigoUsuario")
    private String user;

    public int getNici() {

        return nici;
    }

    public String getTicket() {

        return ticket;
    }

    public String getUser() {

        return user;
    }

}
