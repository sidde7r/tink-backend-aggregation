package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.rpc.GenericRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JacksonXmlRootElement(localName = "GetSyntheseCpteAbonnement")
public class AccountsRequest extends GenericRequest {

    @Override
    public String action() {
        return CaisseEpargneConstants.SoapAction.GET_SYNTHESE_CPTE_ABONNEMENT;
    }
}
