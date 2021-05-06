package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.common.model.CategoryEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
public class TransactionRequest {
    private final String accountId;

    private final boolean isPendingRequired;

    private final CategoryEntity category;

    private final String currencyCode;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime transactionStartDate;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime transactionEndDate;
}
