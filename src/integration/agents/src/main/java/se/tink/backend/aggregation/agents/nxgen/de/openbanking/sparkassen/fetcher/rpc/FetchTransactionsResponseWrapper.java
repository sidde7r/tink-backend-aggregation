package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml.FetchTransactionsResponse;

@XmlRootElement(name = "Documents")
public class FetchTransactionsResponseWrapper {

    @XmlElement(name = "Document")
    private List<FetchTransactionsResponse> fetchTransactionsResponses;

    public List<FetchTransactionsResponse> getFetchTransactionsResponses() {
        return fetchTransactionsResponses;
    }
}
