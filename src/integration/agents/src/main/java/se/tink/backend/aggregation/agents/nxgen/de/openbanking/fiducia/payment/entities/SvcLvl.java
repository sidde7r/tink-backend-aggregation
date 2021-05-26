package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities;

import javax.xml.bind.annotation.XmlElement;

public class SvcLvl {
    @XmlElement(name = "Cd")
    private String cd;

    public SvcLvl() {}

    public SvcLvl(String cd) {
        this.cd = cd;
    }
}
