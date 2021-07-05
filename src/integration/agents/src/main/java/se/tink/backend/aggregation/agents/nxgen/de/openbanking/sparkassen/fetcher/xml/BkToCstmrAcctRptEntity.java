package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import jakarta.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class BkToCstmrAcctRptEntity {
    @XmlElement(name = "GrpHdr")
    private GroupHeaderEntity groupHeader;

    @XmlElement(name = "Rpt")
    private List<RptEntity> rpt = new ArrayList<>();

    public List<RptEntity> getRpt() {
        return rpt;
    }
}
