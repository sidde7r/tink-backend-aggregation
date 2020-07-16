package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.rpc.GenericRequest;

@JacksonXmlRootElement(localName = "sso_BAPI")
@JsonPropertyOrder({"modeALD", "idTerminal", "etablissement", "at"})
public class SsoBapiRequest extends GenericRequest {

    public SsoBapiRequest(String accessToken, String idTerminal) {
        this.accessToken = accessToken;
        this.idTerminal = idTerminal;
    }

    @JacksonXmlProperty(isAttribute = true)
    private String xmlns = "http://caisse-epargne.fr/webservices/";

    @JacksonXmlProperty(localName = "modeALD")
    private int modeAld = 0;

    @JacksonXmlProperty(localName = "idTerminal")
    private String idTerminal;

    @JacksonXmlProperty(localName = "etablissement")
    private String establishment = CaisseEpargneConstants.RequestValues.CAISSE_EPARGNE;

    @JacksonXmlProperty(localName = "at")
    private String accessToken;

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setIdTerminal(String idTerminal) {
        this.idTerminal = idTerminal;
    }

    @Override
    @JsonIgnore
    public String soapAction() {
        return HeaderValues.SSO_BAPI;
    }
}
