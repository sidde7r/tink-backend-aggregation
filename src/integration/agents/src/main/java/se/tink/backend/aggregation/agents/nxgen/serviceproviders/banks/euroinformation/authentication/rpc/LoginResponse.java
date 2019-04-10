package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc;

import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationMsgDateDeserializer;

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class LoginResponse {

    @XmlElement(name = "code_retour")
    private String returnCode;

    @XmlJavaTypeAdapter(EuroInformationMsgDateDeserializer.class)
    @XmlElement(name = "dtcnx")
    private Date date;

    private String typctr;
    private String rib;
    private String fede;
    private String urlfede;
    private String userid;
    private String cin;

    @XmlElementWrapper(name = "domain_list")
    @XmlElement(name = "domain")
    private List<String> domainList;

    public String getReturnCode() {
        return returnCode;
    }

    public String getTypctr() {
        return typctr;
    }

    public String getRib() {
        return rib;
    }

    public String getFede() {
        return fede;
    }

    public String getUrlfede() {
        return urlfede;
    }

    public String getUserid() {
        return userid;
    }

    public String getCin() {
        return cin;
    }

    public List<String> getDomainList() {
        return domainList;
    }
}
