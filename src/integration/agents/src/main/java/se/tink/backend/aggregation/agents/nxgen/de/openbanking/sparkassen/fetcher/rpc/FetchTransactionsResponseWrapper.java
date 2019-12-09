package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@XmlRootElement(name = "Documents")
public class FetchTransactionsResponseWrapper {

    @XmlElement(name = "Document")
    private List<FetchTransactionsResponse> fetchTransactionsResponses;

    @JsonIgnore
    public List<AggregationTransaction> toTinkTransactions() {
        return Optional.ofNullable(fetchTransactionsResponses).orElse(Collections.emptyList())
                .stream()
                .map(FetchTransactionsResponse::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
