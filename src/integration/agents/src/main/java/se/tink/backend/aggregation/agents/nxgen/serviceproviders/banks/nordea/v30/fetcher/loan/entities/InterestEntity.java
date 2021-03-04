package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class InterestEntity {
    private BigDecimal rate;
    private String referenceRateType;
    private String periodStartDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate discountedRateEndDate;

    private BigDecimal baseRate;

    public BigDecimal getTinkInterestRate() {
        return AgentParsingUtils.parsePercentageFormInterest(rate);
    }
}
