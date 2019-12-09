package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml;

import javax.xml.bind.annotation.XmlElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
// XML: Bal
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
