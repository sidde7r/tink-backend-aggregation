package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "root")
public class LogoutResponse {

    @XmlElement(name = "code_retour")
    private String returnCode;

    public String getReturnCode() {
        return returnCode;
    }
}
