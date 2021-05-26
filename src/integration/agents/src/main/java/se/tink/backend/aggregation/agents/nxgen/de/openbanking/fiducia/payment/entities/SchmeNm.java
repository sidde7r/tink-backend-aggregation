package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities;

import javax.xml.bind.annotation.XmlElement;

public class SchmeNm {
    @XmlElement(name = "Prptry")
    private String prptry;

    public SchmeNm() {}

    public SchmeNm(String prptry) {
        this.prptry = prptry;
    }
}
