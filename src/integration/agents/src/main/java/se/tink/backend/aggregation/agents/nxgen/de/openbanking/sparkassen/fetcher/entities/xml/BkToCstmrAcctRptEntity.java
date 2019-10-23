package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml;

import javax.xml.bind.annotation.XmlElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BkToCstmrAcctRptEntity {
    @XmlElement(name = "GrpHdr")
    private GroupHeaderEntity groupHeader;

    @XmlElement(name = "Rpt")
    private RptEntity rpt;

    public RptEntity getRpt() {
        return rpt;
    }
}
