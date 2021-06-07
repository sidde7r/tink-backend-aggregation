package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import javax.xml.bind.annotation.XmlElement;

public class DbtrAgt {

    @XmlElement(name = "FinInstnId")
    private FinInstnId finInstnId;

    public DbtrAgt() {}

    public DbtrAgt(FinInstnId finInstnId) {
        this.finInstnId = finInstnId;
    }
}
