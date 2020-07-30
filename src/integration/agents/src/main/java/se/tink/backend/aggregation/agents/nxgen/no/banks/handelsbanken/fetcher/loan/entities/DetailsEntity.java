package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.entities;

import java.math.BigDecimal;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class DetailsEntity {
    private long eventDate;
    private String eventType;
    private BigDecimal interest;
    private BigDecimal instalment;
    private BigDecimal charges;
    private BigDecimal balance;
    private BigDecimal payment;
    private boolean annualDetails;
}
