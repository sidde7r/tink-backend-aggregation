package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class BalanceDto {

    private BigDecimal lastStatementBalanceAmount;

    private BigDecimal debitsBalanceAmount;

    private BigDecimal paymentsCreditsAmount;

    private BigDecimal statementBalanceAmount;

    private BigDecimal paymentDueAmount;

    private String isoAlphaCurrencyCode;

    private BigDecimal remainingStatementBalanceAmount;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate paymentDueDate;
}
