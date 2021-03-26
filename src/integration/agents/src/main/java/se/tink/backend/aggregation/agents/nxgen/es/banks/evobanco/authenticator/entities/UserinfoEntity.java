package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserinfoEntity {
    @JsonProperty("telefonoMovil")
    private String mobilePhone;

    @JsonProperty("codigoTarifa")
    private String rateCode;

    private String idInternoPe;

    @JsonProperty("usuarioHCE")
    private String userHCE;

    @JsonProperty("codigoEntidad")
    private String entityCode;

    @JsonProperty("numTarjeta")
    private String numCard;

    @JsonProperty("numIntentos")
    private String numAttempts;

    @JsonProperty("nombreCliente")
    private String clientName;

    @JsonProperty("estadoUsuario")
    private String userStatus;

    @JsonProperty("oficina")
    private String office;

    @JsonProperty("usuarioBE")
    private String userBE;

    @JsonProperty("acuerdoBE")
    private String agreementBE;

    @JsonProperty("apellido1Cliente")
    private String surname1Client;

    @JsonProperty("tipoUsuario")
    private String userType;

    @JsonProperty("usuario")
    private String user;

    @JsonProperty("ecvPersonaAcuerdo")
    private String ecvPersonAgreement;

    @JsonProperty("codigoIdExterno")
    private String externalIdCode;

    @JsonProperty("idExterno")
    private String externalId;

    private String id;

    @JsonProperty("fechaUltimaConexion")
    private String dateLastConnection;

    @JsonProperty("apellido2Cliente")
    private String surname2Client;

    private String mail;

    private AgreementsEntity agreements;

    public String getEntityCode() {
        return entityCode;
    }

    public String getUserBE() {
        return userBE;
    }

    public String getAgreementBE() {
        return agreementBE;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public String getClientName() {
        return clientName;
    }

    public String getSurname1Client() {
        return surname1Client;
    }

    public String getSurname2Client() {
        return surname2Client;
    }
}
