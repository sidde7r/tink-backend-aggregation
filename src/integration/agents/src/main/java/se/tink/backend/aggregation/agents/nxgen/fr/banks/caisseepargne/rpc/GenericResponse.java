package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants;

public class GenericResponse<T> {

    @JacksonXmlProperty(localName = "Resultat")
    protected T results;

    @JacksonXmlProperty(localName = "CodeRetour")
    private String returnCode;

    @JacksonXmlProperty(localName = "LibelleRetour")
    private String returnDescription;

    public boolean isResponseOK() {
        return CaisseEpargneConstants.ResponseValue.RETURN_CODE_OK.equalsIgnoreCase(returnCode);
    }
}
