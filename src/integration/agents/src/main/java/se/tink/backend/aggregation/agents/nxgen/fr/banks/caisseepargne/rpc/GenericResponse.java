package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.ResponseValues;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericResponse<T> {
    @JacksonXmlProperty(localName = "Resultat")
    protected T results;

    @JacksonXmlProperty(localName = "CodeRetour")
    private String returnCode;

    @JacksonXmlProperty(localName = "LibelleRetour")
    private String returnDescription;

    public boolean isResponseOK() {
        return ResponseValues.RETURN_CODE_OK.equalsIgnoreCase(returnCode);
    }
}
