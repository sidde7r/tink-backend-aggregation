package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities;

import javax.xml.bind.annotation.XmlElement;

public class Amt {
    @XmlElement(name = "InstdAmt")
    private InstdAmt instdAmt;

    public Amt() {}

    public Amt(InstdAmt instdAmt) {
        this.instdAmt = instdAmt;
    }

    public InstdAmt getInstdAmt() {
        return instdAmt;
    }
}
