package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import jakarta.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

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
    private List<EntryEntity> entries = new ArrayList<>();

    public List<BalanceEntity> getBalances() {
        return balances;
    }

    public List<EntryEntity> getEntries() {
        return entries;
    }
}
