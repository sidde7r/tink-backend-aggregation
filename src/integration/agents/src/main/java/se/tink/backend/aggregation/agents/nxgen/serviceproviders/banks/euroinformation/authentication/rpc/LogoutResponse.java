package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "root")
public class LogoutResponse {

    @XmlElement(name = "code_retour")
    private String returnCode;

    public String getReturnCode() {
        return returnCode;
    }
}
