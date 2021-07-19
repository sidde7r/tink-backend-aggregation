package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session.rpc;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session.entities.InitializationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationMsgDateDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class PfmInitResponse {
    @XmlElement(name = "code_retour")
    private String returnCode;

    @XmlElement(name = "date_msg")
    @XmlJavaTypeAdapter(EuroInformationMsgDateDeserializer.class)
    private Date date;

    @XmlElement(name = "initialization")
    private InitializationEntity initialization;

    public InitializationEntity getInitialization() {
        return initialization;
    }

    public String getReturnCode() {
        return returnCode;
    }
}
