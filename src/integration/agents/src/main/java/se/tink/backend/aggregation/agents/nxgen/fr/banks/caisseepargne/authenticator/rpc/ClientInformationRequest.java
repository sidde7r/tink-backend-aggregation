package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.rpc.GenericRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JacksonXmlRootElement(localName = "GetInfosClient")
public class ClientInformationRequest extends GenericRequest {

    @Override
    public String action() {
        return CaisseEpargneConstants.SoapAction.GET_INFOS_CLIENT;
    }
}
