package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.PaginationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class AccountTransactionsPaginationEntity {

    @JsonProperty("dataList")
    private List<AccountTransactionEntity> transactions;

    private PaginationEntity pagination;
}
