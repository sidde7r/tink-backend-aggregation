package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Document")
public class CreatePaymentXmlRequest {

    @XmlElement(name = "CstmrCdtTrfInitn")
    private CstmrCdtTrfInitn cstmrCdtTrfInitn;

    public CreatePaymentXmlRequest() {}

    public CreatePaymentXmlRequest(CstmrCdtTrfInitn cstmrCdtTrfInitn) {
        this.cstmrCdtTrfInitn = cstmrCdtTrfInitn;
    }
}
