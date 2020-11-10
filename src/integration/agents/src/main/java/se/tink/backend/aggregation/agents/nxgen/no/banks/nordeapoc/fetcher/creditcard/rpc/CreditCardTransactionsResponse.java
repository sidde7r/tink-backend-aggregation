package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.creditcard.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.creditcard.entity.CreditCardTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class CreditCardTransactionsResponse {
    private int page;
    private int pageSize;
    private int size;
    private List<CreditCardTransactionEntity> transactions;
}
