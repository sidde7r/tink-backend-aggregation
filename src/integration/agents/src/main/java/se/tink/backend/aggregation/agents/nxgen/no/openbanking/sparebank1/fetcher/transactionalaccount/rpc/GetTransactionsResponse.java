package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetTransactionsResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private List<TransactionEntity> transactions;

    public List<TransactionEntity> getTransactions() {
        return Optional.ofNullable(transactions).orElse(Collections.emptyList());
    }
}
