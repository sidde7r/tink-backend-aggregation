package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import jakarta.xml.bind.annotation.XmlElement;

public class FinInstnId {

    @XmlElement(name = "BIC")
    private String bic;

    @XmlElement(name = "Othr")
    private Othr othr;

    public FinInstnId() {}

    public FinInstnId(Othr othr) {
        this.othr = othr;
    }

    public FinInstnId(String bic) {
        this.bic = bic;
    }
}
