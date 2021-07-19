package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import jakarta.xml.bind.annotation.XmlElement;

public class Cdtr {
    @XmlElement(name = "Nm")
    private String nm;

    public Cdtr() {}

    public Cdtr(String nm) {
        this.nm = nm;
    }
}
