package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.rpc.GenericRequest;

@JacksonXmlRootElement(localName = "GetRice")
@JsonPropertyOrder({"cpt"})
public class AccountDetailsRequest extends GenericRequest {

    public AccountDetailsRequest(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @JacksonXmlProperty(isAttribute = true)
    private String xmlns = "http://caisse-epargne.fr/webservices/";

    @JacksonXmlProperty(localName = "cpt")
    private String accountNumber;

    @Override
    @JsonIgnore
    public String soapAction() {
        return HeaderValues.GET_ACCOUNT_DETAILS;
    }
}
