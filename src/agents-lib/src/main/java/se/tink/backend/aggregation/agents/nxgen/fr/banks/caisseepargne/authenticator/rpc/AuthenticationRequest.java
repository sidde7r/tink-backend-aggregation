package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.rpc.GenericRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JacksonXmlRootElement(localName = "Authentifier")
@JsonPropertyOrder({
        "numeroAbonne",
        "password",
        "numUsager",
        "niveauService",
        "modeALD",
        "idTerminal",
        "etablissement",
        "apiKey"
})
public class AuthenticationRequest extends GenericRequest {

    @JacksonXmlProperty(localName = "numeroAbonne")
    private String username;
    private String password;
    private String numUsager = null;
    private String niveauService = "PAR";
    private int modeALD = 0;
    @JacksonXmlProperty(localName = "idTerminal")
    private String deviceId;
    private String etablissement = "CAISSE_EPARGNE";
    private String apiKey = null;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String action() {
        return "http://caisse-epargne.fr/webservices/Authentifier";
    }
}
