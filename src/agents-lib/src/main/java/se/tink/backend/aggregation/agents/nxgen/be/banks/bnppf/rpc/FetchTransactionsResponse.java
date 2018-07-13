package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities.Link;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities.Meta;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities.TransactionData;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchTransactionsResponse {
    private TransactionData data;
    private Link links;
    private Meta meta;

    public TransactionData getData() {
        return data;
    }

    public Link getLinks() {
        return links;
    }

    public Meta getMeta() {
        return meta;
    }
}
