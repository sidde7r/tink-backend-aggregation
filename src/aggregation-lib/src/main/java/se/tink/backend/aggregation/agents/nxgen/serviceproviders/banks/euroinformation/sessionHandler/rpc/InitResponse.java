package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.sessionHandler.rpc;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationMsgDateDeserializer;

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class InitResponse {
    @XmlElement(name = "code_retour")
    private String returnCode;
    @XmlElement(name = "date_msg")
    @XmlJavaTypeAdapter(EuroInformationMsgDateDeserializer.class)
    private Date date;

    public String getReturnCode() {
        return returnCode;
    }
}
