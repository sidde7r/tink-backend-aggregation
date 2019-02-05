package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Authentication")
public class Authentication {
    private String Pin;

    public String getPin() {
        return Pin;
    }

    @XmlElement(name = "Pin")
    public void setPin(String Pin) {
        this.Pin = Pin;
    }
}