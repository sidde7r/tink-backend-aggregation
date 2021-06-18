package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.response;

import javax.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CstmrPmtStsRpt {

    @XmlElement(name = "OrgnlGrpInfAndSts")
    private OrgnlGrpInfAndSts orgnlGrpInfAndSts;
}
