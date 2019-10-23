package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RptEntity {
    @XmlElement(name = "Id")
    private String id;

    @XmlElement(name = "ElctrncSeqNb")
    private String elctrncSeqNb;

    @XmlElement(name = "CreDtTm")
    private String creDtTm;

    @XmlElement(name = "Acct")
    private AccountEntity account;

    @XmlElement(name = "Bal")
    private List<BalanceEntity> balances;

    @XmlElement(name = "Ntry")
    private List<EntryEntity> entries;

    public List<BalanceEntity> getBalances() {
        return balances;
    }

    public List<EntryEntity> getEntries() {
        return entries;
    }
}
