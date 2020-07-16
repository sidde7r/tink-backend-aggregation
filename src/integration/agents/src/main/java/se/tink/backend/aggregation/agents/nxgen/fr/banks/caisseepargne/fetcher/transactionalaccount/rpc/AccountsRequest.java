package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.rpc.GenericRequest;

@JacksonXmlRootElement(localName = "GetSyntheseCpteAbonnement")
@JsonPropertyOrder
public class AccountsRequest extends GenericRequest {

    @JacksonXmlProperty(isAttribute = true)
    private String xmlns = "http://caisse-epargne.fr/webservices/";

    @Override
    @JsonIgnore
    public String soapAction() {
        return HeaderValues.GET_ACCOUNTS;
    }
}
