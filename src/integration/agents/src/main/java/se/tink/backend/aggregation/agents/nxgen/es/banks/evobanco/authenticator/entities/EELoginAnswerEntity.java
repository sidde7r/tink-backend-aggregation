package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EELoginAnswerEntity {
    private String mail;

    @JsonProperty("fechaUltimaConexion")
    private String dateLastConnection;

    @JsonProperty("numTarjeta")
    private String numCard;

    @JsonProperty("nombreCliente")
    private String clientName;

    @JsonProperty("oficina")
    private String office;

    @JsonProperty("apellido1Cliente")
    private String surname1Client;

    @JsonProperty("estadoUsuario")
    private String userStatus;

    @JsonProperty("ListaAcuerdosBE")
    private List<AgreementsListBEEntity> agreementsListbe;

    @JsonProperty("tipoUsuario")
    private String userType;

    @JsonProperty("numIntentos")
    private String inAnAttempt;

    @JsonProperty("codigoIdExterno")
    private String externalIdCode;

    @JsonProperty("usuarioBE")
    private String userbe;

    @JsonProperty("idInternoPe")
    private String internalIdPe;

    @JsonProperty("apellido2Cliente")
    private String surname2Client;

    private String idExterno;

    @JsonProperty("codigoEntidad")
    private String entityCode;

    @JsonProperty("telefonoMovil")
    private String mobilePhone;

    public String getInternalIdPe() {
        return internalIdPe;
    }
}
