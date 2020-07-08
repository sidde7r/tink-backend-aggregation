package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.ResponseValue;

@Getter
public class GenericResponse<T> {
    @JacksonXmlProperty(localName = "Resultat")
    protected T results;

    @JacksonXmlProperty(localName = "CodeRetour")
    private String returnCode;

    @JacksonXmlProperty(localName = "LibelleRetour")
    private String returnDescription;

    public boolean isResponseOK() {
        return ResponseValue.RETURN_CODE_OK.equalsIgnoreCase(returnCode);
    }
}
