package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import javax.xml.bind.annotation.XmlElement;

public class GetAccountStatementItemsRequestEntity {
    private String serverSessionID;
    private Context context;
    private String qid;
    private ProductID productID;
    StatementSearchCriteria statementSearchCriteria;

    @XmlElement(name = "ServerSessionID")
    public void setServerSessionID(String serverSessionID) {
        this.serverSessionID = serverSessionID;
    }

    public String getServerSessionID() {
        return serverSessionID;
    }

    @XmlElement(name = "Context")
    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @XmlElement(name = "QID")
    public void setQid(String qid) {
        this.qid = qid;
    }

    public String getQid() {
        return qid;
    }

    @XmlElement(name = "ProductId")
    public void setProductID(ProductID productID) {
        this.productID = productID;
    }

    public ProductID getProductID() {
        return productID;
    }

    @XmlElement(name = "StatementSearchCriteria")
    public void setStatementSearchCriteria(StatementSearchCriteria statementSearchCriteria) {
        this.statementSearchCriteria = statementSearchCriteria;
    }

    public StatementSearchCriteria getStatementSearchCriteria() {
        return statementSearchCriteria;
    }
}
