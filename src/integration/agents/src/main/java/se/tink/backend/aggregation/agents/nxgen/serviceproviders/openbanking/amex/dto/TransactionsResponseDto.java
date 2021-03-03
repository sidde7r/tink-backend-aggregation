package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class TransactionsResponseDto {

    private Integer totalTransactionCount;

    private List<TransactionDto> transactions;

    public Integer getTotalTransactionCount() {
        return totalTransactionCount;
    }

    public List<TransactionDto> getTransactions() {
        return transactions;
    }
}
