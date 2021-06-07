package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import javax.xml.bind.annotation.XmlElement;

public class CdtrAcct {
    @XmlElement(name = "Id")
    private IbanId id;

    public CdtrAcct() {}

    public CdtrAcct(IbanId id) {
        this.id = id;
    }

    public IbanId getId() {
        return id;
    }
}
