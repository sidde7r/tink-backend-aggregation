package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.PaginationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class AccountTransactionsPaginationEntity {

    @JsonProperty("dataList")
    private List<AccountTransactionEntity> transactions;

    private PaginationEntity pagination;

    @JsonIgnore
    public static AccountTransactionsPaginationEntity createEmptyAccountTransactionsEntity() {
        AccountTransactionsPaginationEntity entity = new AccountTransactionsPaginationEntity();
        entity.setPagination(new PaginationEntity());
        entity.setTransactions(Collections.emptyList());
        return entity;
    }
}
