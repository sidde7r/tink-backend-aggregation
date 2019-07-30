package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ns1:GetAccountInformationListResponse")
public class GetAccountInformationListResponseEntity {
    private OK ok;

    @XmlElement(name = "OK")
    public void setOk(OK ok) {
        this.ok = ok;
    }

    public OK getOk() {
        return ok;
    }
}
