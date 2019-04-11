package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants;
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

    @JacksonXmlProperty(localName = "password")
    private String password;

    @JacksonXmlProperty(localName = "numUsager")
    private String numUsager = null;

    @JacksonXmlProperty(localName = "niveauService")
    private String serviceLevel = CaisseEpargneConstants.RequestValue.SERVICE_LEVEL;

    @JacksonXmlProperty(localName = "modeALD")
    private int modeALD = CaisseEpargneConstants.RequestValue.MODE_ALD;

    @JacksonXmlProperty(localName = "idTerminal")
    private String deviceId;

    @JacksonXmlProperty(localName = "etablissement")
    private String institution = CaisseEpargneConstants.RequestValue.INSTITUTION;

    @JacksonXmlProperty(localName = "apiKey")
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

    public String getUsername() {
        return username;
    }

    @Override
    public String action() {
        return CaisseEpargneConstants.SoapAction.AUTHENTIFIER;
    }
}
