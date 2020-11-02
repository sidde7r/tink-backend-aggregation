package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionResponse {

    @JsonProperty("Nombre")
    private String name;

    @JsonProperty("NIF")
    private String nif;

    @JsonProperty("TipoUsuario")
    private int userType;

    @JsonProperty("NICI")
    private String nici;

    @JsonProperty("Ticket")
    private String ticket;

    @JsonProperty("CodigoUsuario")
    private String user;

    @JsonProperty("ContratoEnCurso")
    private String contractInCourse;

    @JsonProperty("NIP")
    private String nip;

    @JsonProperty("ValidacionSCA")
    private boolean validationSCA;

    @JsonProperty("Telefono")
    private String phone;

    @JsonProperty("TokenIdentity")
    private String tokenIdentity;

    public String getName() {
        return name;
    }

    public String getNif() {
        return nif;
    }

    public int getUserType() {
        return userType;
    }

    public String getNici() {

        return nici;
    }

    public String getTicket() {

        return ticket;
    }

    public String getUser() {

        return user;
    }

    public String getContractInCourse() {
        return contractInCourse;
    }

    public String getNip() {
        return nip;
    }

    public String getTokenIdentity() {
        return tokenIdentity;
    }
}
