package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class AccountTransactionResponse implements PaginatorResponse {

    private List<TransactionEntity> transactions;
    private boolean moreDataAvaliable;

    // Using this setter to avoid creating several layers of wrapper entities because of JSON tree structure
    @JsonProperty("listaMovimientos")
    private void unpackNested(JsonNode m) throws IOException{

        m = m.get("listaMovimientos"); // Skip one duplicate level of the JSON tree

        moreDataAvaliable = m.get("masDatos").asBoolean();
        transactions = new ObjectMapper().readValue(m.get("movimiento").traverse(),
                new TypeReference <List<TransactionEntity>>(){});
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(moreDataAvaliable);
    }
}
