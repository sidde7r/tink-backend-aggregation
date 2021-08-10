package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import jakarta.xml.bind.annotation.XmlElement;

public class Othr {
    @XmlElement(name = "Id")
    private String id;

    @XmlElement(name = "SchmeNm")
    private SchmeNm schmeNm;

    public Othr() {}

    public Othr(String id, SchmeNm schmeNm) {
        this.id = id;
        this.schmeNm = schmeNm;
    }
}
