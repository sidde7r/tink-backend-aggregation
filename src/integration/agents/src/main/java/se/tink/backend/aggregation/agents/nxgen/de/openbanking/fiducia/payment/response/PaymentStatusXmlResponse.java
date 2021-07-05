package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.response;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
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
