package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import javax.xml.bind.annotation.XmlElement;

public class IbanId {
    @XmlElement(name = "IBAN")
    private String iban;

    public IbanId() {}

    public IbanId(String iban) {
        this.iban = iban;
    }

    public String getIban() {
        return iban;
    }
}
