package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities;

import jakarta.xml.bind.annotation.XmlElement;

public class Fault {
    String faultString;

    @XmlElement(name = "faultstring")
    public void setFaultString(String faultString) {
        this.faultString = faultString;
    }

    public String getFaultString() {
        return faultString;
    }
}
