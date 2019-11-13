package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsDataEntity {
    private Long numberTransactionsAccounting;
    private Long numberTransactionsNotAccounting;
    private String updateDate;
    private String updateHour;
    private AmountEntity totalOutput;
    private AmountEntity totalEnter;
    private List<TransactionEntity> transactionsAccounting;
    private List<TransactionEntity> transactionsNotAccounting;

    @JsonIgnore
    public Collection<? extends Transaction> toTinkTransactions() {
        return Stream.concat(
                        Optional.ofNullable(transactionsAccounting).orElse(Collections.emptyList())
                                .stream()
                                .map(TransactionEntity::toBookedTransaction),
                        Optional.ofNullable(transactionsNotAccounting)
                                .orElse(Collections.emptyList()).stream()
                                .map(TransactionEntity::toPendingTransactions))
                .collect(Collectors.toList());
    }
}
