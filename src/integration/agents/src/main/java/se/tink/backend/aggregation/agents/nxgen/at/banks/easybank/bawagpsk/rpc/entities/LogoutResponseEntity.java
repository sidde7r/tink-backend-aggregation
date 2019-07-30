package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities;

import javax.xml.bind.annotation.XmlElement;

public class LogoutResponseEntity {
    private OK ok;

    @XmlElement(name = "OK")
    public void setOk(OK ok) {
        this.ok = ok;
    }

    public OK getOk() {
        return ok;
    }
}
