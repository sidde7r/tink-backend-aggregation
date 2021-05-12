package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionsDataEntity {
    private Long numberTransactionsAccounting;
    private Long numberTransactionsNotAccounting;
    private Long nextAccounting;
    private Long nextNotAccounting;
    private String updateDate;
    private String updateHour;
    private AmountEntity totalOutput;
    private AmountEntity totalEnter;
    private List<TransactionEntity> transactionsAccounting;
    private List<TransactionEntity> transactionsNotAccounting;
}
