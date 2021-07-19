package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import jakarta.xml.bind.annotation.XmlElement;

public class OrgId {
    @XmlElement(name = "Othr")
    private Othr othr;

    public OrgId() {}

    public OrgId(Othr othr) {
        this.othr = othr;
    }
}
