package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities;

import javax.xml.bind.annotation.XmlElement;

public class ResponseMessage {
    private String code;

    @XmlElement(name = "Code")
    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
