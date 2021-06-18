package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import javax.xml.bind.annotation.XmlElement;

public class SchmeNm {
    @XmlElement(name = "Prtry")
    private String prtry;

    public SchmeNm() {}

    public SchmeNm(String prtry) {
        this.prtry = prtry;
    }
}
