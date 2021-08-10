package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import jakarta.xml.bind.annotation.XmlElement;

public class InitgPty {
    @XmlElement(name = "Id")
    private Id id;

    public InitgPty() {}

    public InitgPty(Id id) {
        this.id = id;
    }
}
