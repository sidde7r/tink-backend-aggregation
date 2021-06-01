package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities;

import javax.xml.bind.annotation.XmlElement;

public class Dbtr {
    @XmlElement(name = "Nm")
    private String nm;

    public Dbtr() {}

    public Dbtr(String nm) {
        this.nm = nm;
    }
}
