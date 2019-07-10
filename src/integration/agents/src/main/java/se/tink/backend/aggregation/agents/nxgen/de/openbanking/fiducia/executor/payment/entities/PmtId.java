package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities;

import javax.xml.bind.annotation.XmlElement;

public class PmtId {
    @XmlElement(name = "EndToEndId")
    private String endToEndId;

    public PmtId() {}

    public PmtId(String endToEndId) {
        this.endToEndId = endToEndId;
    }
}
