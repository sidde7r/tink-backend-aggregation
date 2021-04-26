package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.common.model.CategoryEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionRequest {
    private String accountId;

    private boolean isPendingRequired;

    private CategoryEntity category;

    private String currencyCode;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime transactionStartDate;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime transactionEndDate;

    public TransactionRequest setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public TransactionRequest setPendingRequired(boolean pendingRequired) {
        isPendingRequired = pendingRequired;
        return this;
    }

    public TransactionRequest setCategory(CategoryEntity category) {
        this.category = category;
        return this;
    }

    public TransactionRequest setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
        return this;
    }

    public TransactionRequest setTransactionStartDate(LocalDateTime transactionStartDate) {
        this.transactionStartDate = transactionStartDate;
        return this;
    }

    public TransactionRequest setTransactionEndDate(LocalDateTime transactionEndDate) {
        this.transactionEndDate = transactionEndDate;
        return this;
    }
}
