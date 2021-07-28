package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class TransactionEntity {
    private BigDecimal amount;
    private boolean booked;

    private LocalDate bookingDate;

    private LocalDate transactionDate;

    private String description;
    private String message;
}
