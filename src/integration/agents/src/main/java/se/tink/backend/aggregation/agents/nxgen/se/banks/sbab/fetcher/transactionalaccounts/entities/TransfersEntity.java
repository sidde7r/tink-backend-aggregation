package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransfersEntity {
    private List<LinksEntity> links;
    private List<TransactionsEntity> transactions;

    public List<LinksEntity> getLinks() {
        return links;
    }

    public List<TransactionsEntity> getTransactions() {
        return transactions;
    }
}
