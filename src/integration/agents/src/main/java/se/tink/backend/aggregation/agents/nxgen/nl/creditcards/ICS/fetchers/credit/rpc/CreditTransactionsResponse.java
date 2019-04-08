package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.CreditDataEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.MetaEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class CreditTransactionsResponse implements PaginatorResponse {
    @JsonProperty("Data")
    private CreditDataEntity data;

    @JsonProperty("Links")
    private LinksEntity links;

    @JsonProperty("Meta")
    private MetaEntity meta;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return data.getTransactions().stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    /**
     * Currently ICS does not support pagination. Once they do, this should be implemented
     *
     * @return
     */
    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(false);
    }
}
