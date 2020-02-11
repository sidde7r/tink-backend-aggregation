package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml.FetchTransactionsResponse;

@XmlRootElement(name = "Documents")
public class FetchTransactionsResponseWrapper {

    @XmlElement(name = "Document")
    private List<FetchTransactionsResponse> fetchTransactionsResponses;

    public List<FetchTransactionsResponse> getFetchTransactionsResponses() {
        return fetchTransactionsResponses;
    }
}
