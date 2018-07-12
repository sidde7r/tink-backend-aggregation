package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "OK")
public class OK {
    // TODO doesn't scale well as all but one of these fields are null
    private String serverSessionID;
    private String qid;
    private Disposer disposer;
    private List<AccountInformationListItem> accountInformationListItemList;
    private List<AccountStatementItem> accountStatementItemList;

    @XmlElement(name = "ServerSessionID")
    public void setServerSessionID(String serverSessionID) {
        this.serverSessionID = serverSessionID;
    }

    public String getServerSessionID() {
        return serverSessionID;
    }

    @XmlElement(name = "Disposer")
    public void setDisposer(Disposer disposer) {
        this.disposer = disposer;
    }

    public Disposer getDisposer() {
        return disposer;
    }

    @XmlElement(name = "AccountInformationListItem")
    public void setAccountInformationListItemList(List<AccountInformationListItem> accountInformationListItemList) {
        this.accountInformationListItemList = accountInformationListItemList;
    }

    public List<AccountInformationListItem> getAccountInformationListItemList() {
        return accountInformationListItemList;
    }

    @XmlElement(name = "QID")
    public void setQid(String qid) {
        this.qid = qid;
    }

    public String getQid() {
        return qid;
    }

    @XmlElement(name = "AccountStatementItem")
    public void setAccountStatementItemList(List<AccountStatementItem> accountStatementItemList) {
        this.accountStatementItemList = accountStatementItemList;
    }

    public List<AccountStatementItem> getAccountStatementItemList() {
        return accountStatementItemList;
    }
}
