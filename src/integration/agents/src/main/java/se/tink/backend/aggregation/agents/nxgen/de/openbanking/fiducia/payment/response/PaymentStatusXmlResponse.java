package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Document")
public class PaymentStatusXmlResponse {

    @XmlElement(name = "CstmrPmtStsRpt")
    private CstmrPmtStsRpt cstmrPmtStsRpt;
}
