package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.rpc.GenericRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JacksonXmlRootElement(localName = "GetHistoriqueOperationsByCompte")
@JsonPropertyOrder({"cpt", "nbOp", "typeDemandeNav", "bufferNav"})
public class TransactionsRequest extends GenericRequest {

    @JacksonXmlProperty(isAttribute = true)
    private String xmlns = "http://caisse-epargne.fr/webservices/";

    @JacksonXmlProperty(localName = "cpt")
    private String account;

    @JacksonXmlProperty(localName = "nbOp")
    private int pageSize = CaisseEpargneConstants.RequestValues.PAGE_SIZE;

    @JacksonXmlProperty(localName = "bufferNav")
    private String paginationKey;

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String soapAction() {
        return HeaderValues.GET_TRANSACTIONS;
    }

    public void setPaginationKey(String paginationKey) {
        this.paginationKey = paginationKey;
    }

    @JacksonXmlProperty(localName = "typeDemandeNav")
    public String getRequestType() {
        return Strings.isNullOrEmpty(paginationKey)
                ? CaisseEpargneConstants.RequestValues.TRANSACTION_REQUEST_TYPE_INITIAL
                : CaisseEpargneConstants.RequestValues.TRANSACTION_REQUEST_TYPE_SUBSEQUENT;
    }
}
