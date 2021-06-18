package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import javax.xml.bind.annotation.XmlElement;

public class RmtInf {
    @XmlElement(name = "Ustrd")
    private String ustrd;

    public RmtInf() {}

    public RmtInf(String ustrd) {
        this.ustrd = ustrd;
    }
}
