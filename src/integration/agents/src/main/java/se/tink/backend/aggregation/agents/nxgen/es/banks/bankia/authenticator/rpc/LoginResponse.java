package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse {
    @JsonProperty("resultadoCorrecto")
    private boolean correctResult;

    @JsonProperty("resultadoMensaje")
    private String resultMessage;

    @JsonProperty("nombreCompletoUsuario")
    private String fullNameUser;

    @JsonProperty("esMulticanal")
    private boolean isMultichannel;

    @JsonProperty("catalogacionCliente")
    private String customerCataloging;

    @JsonProperty("segmentoEtiquetado")
    private String segmentLabeling;

    private String ursusHash;

    @JsonProperty("mostrarZonaGestorAceptacionApps")
    private boolean showZoneManagerAcceptanceApps;

    @JsonProperty("mostrarBandejaDeTareasPG")
    private boolean showTaskTrayPg;

    @JsonProperty("mostrarGestorBpApps")
    private boolean showGestorBpApps;

    @JsonProperty("nombreUsuario")
    private String username;

    @JsonProperty("apellido1Usuario")
    private String surname1User;

    @JsonProperty("apellido2Usuario")
    private String surname2User;

    @JsonProperty("tramoEdad")
    private int ageSection;

    @JsonProperty("rangoEdad")
    private String ageRange;

    private String execution;

    public String getExecution() {
        return execution;
    }

    public boolean isCorrectResult() {
        return correctResult;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public String getFullNameUser() {
        return fullNameUser;
    }

    public boolean isMultichannel() {
        return isMultichannel;
    }

    public String getCustomerCataloging() {
        return customerCataloging;
    }

    public String getSegmentLabeling() {
        return segmentLabeling;
    }

    public String getUrsusHash() {
        return ursusHash;
    }

    public boolean isShowZoneManagerAcceptanceApps() {
        return showZoneManagerAcceptanceApps;
    }

    public boolean isShowTaskTrayPg() {
        return showTaskTrayPg;
    }

    public boolean isShowGestorBpApps() {
        return showGestorBpApps;
    }

    public String getUsername() {
        return username;
    }

    public String getSurname1User() {
        return surname1User;
    }

    public String getSurname2User() {
        return surname2User;
    }

    public int getAgeSection() {
        return ageSection;
    }

    public String getAgeRange() {
        return ageRange;
    }
}
