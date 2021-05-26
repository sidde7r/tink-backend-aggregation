package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities;

import javax.xml.bind.annotation.XmlElement;

public class InitgPty {
    @XmlElement(name = "Id")
    private Id id;

    @XmlElement(name = "Nm")
    private String nm;

    public InitgPty() {}

    public InitgPty(Id id, String nm) {
        this.id = id;
        this.nm = nm;
    }
}
