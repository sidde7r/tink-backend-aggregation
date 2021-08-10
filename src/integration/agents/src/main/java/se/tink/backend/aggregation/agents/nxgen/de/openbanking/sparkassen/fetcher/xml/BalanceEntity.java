package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import jakarta.xml.bind.annotation.XmlElement;

public class BalanceEntity {
    @XmlElement(name = "Tp")
    private TpEntity tp;

    @XmlElement(name = "Amt")
    private AmountEntity amount;

    @XmlElement(name = "CdtDbtInd")
    private String cdtDbtInd;

    @XmlElement(name = "Dt")
    private DateEntity date;

    public AmountEntity getAmount() {
        return amount;
    }

    public DateEntity getDate() {
        return date;
    }
}
