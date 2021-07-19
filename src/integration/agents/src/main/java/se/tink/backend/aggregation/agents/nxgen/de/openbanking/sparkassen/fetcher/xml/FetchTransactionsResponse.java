package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Document")
public class FetchTransactionsResponse {

    @XmlElement(name = "BkToCstmrAcctRpt")
    private BkToCstmrAcctRptEntity bkToCstmrAcctRpt;

    public BkToCstmrAcctRptEntity getBkToCstmrAcctRpt() {
        return bkToCstmrAcctRpt;
    }
}
