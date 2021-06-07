package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import javax.xml.bind.annotation.XmlElement;

public class DbtrAcct {
    @XmlElement(name = "Id")
    private IbanId id;

    public DbtrAcct() {}

    public DbtrAcct(IbanId id) {
        this.id = id;
    }

    public IbanId getId() {
        return id;
    }
}
