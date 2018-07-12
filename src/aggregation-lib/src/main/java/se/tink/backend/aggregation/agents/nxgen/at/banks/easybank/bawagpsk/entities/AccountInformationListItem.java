package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "AccountInformationListItem")
public class AccountInformationListItem {
    private AccountInfo accountInfo;

    @XmlElement(name = "AccountInfo")
    public void setAccountInfo(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }
}
