package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;

public class BkToCstmrAcctRptEntity {
    @XmlElement(name = "GrpHdr")
    private GroupHeaderEntity groupHeader;

    @XmlElement(name = "Rpt")
    private List<RptEntity> rpt = new ArrayList<>();

    public List<RptEntity> getRpt() {
        return rpt;
    }
}
