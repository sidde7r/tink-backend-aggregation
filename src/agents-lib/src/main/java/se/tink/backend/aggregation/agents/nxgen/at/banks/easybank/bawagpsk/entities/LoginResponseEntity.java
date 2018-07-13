package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ns1:LoginResponseEntity")
public class LoginResponseEntity {
    private OK ok;
    private Failure failure;

    @XmlElement(name = "OK")
    public void setOk(OK ok) {
        this.ok = ok;
    }

    public OK getOk() {
        return ok;
    }

    @XmlElement(name = "Failure")
    public void setFailure(Failure failure) {
        this.failure = failure;
    }

    public Failure getFailure() {
        return failure;
    }
}
