package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ns4:GetAccountInformationListRequest")
public class GetAccountInformationListRequestEntity {
    private String serverSessionID;
    private Context context;
    private String qid;
    private List<ProductID> productIdList;

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

    @XmlElementWrapper(name = "ProductIdList")
    @XmlElement(name = "ProductId")
    public void setProductIdList(List<ProductID> productIdList) {
        this.productIdList = productIdList;
    }

    public List<ProductID> getProductIdList() {
        return productIdList;
    }

    @XmlElement(name = "QID")
    public void setQid(String qid) {
        this.qid = qid;
    }

    public String getQid() {
        return qid;
    }
}
