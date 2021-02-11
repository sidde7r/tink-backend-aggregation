package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class CardCreditDetails {
    private BigDecimal creditLimit;
    private BigDecimal creditAvailableBalance;
    private String maskedCreditCardNumber;
}
