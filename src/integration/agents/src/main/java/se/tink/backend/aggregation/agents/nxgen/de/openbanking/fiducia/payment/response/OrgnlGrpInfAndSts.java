package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.response;

import javax.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrgnlGrpInfAndSts {
    @XmlElement(name = "OrgnlMsgId")
    private String orgnlMsgId;

    @XmlElement(name = "OrgnlMsgNmId")
    private String orgnlMsgNmId;

    @XmlElement(name = "GrpSts")
    private String grpSts;
}
