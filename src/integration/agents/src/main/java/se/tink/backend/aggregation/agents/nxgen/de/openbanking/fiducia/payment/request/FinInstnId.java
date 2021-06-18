package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import javax.xml.bind.annotation.XmlElement;

public class FinInstnId {

    @XmlElement(name = "Othr")
    private Othr othr;

    public FinInstnId() {}

    public FinInstnId(Othr othr) {
        this.othr = othr;
    }
}
