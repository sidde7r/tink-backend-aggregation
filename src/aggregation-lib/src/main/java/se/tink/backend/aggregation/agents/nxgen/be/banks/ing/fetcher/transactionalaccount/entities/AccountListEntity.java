package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities;

import java.util.List;
import java.util.stream.Stream;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountListEntity {
    @XmlElement(name = "account")
    private List<AccountEntity> accountList;

    public void setAccount(List<AccountEntity> accountList) {
        this.accountList = accountList;
    }

    public Stream<AccountEntity> stream() {
        return this.accountList != null ? this.accountList.stream() : Stream.empty();
    }
}
