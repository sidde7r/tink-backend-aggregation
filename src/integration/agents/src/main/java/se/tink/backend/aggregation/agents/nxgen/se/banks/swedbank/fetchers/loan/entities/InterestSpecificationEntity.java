package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class InterestSpecificationEntity {
    private AmountEntity interest;
    private AmountEntity amount;
    private String interestRate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private String periodDateFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private String periodDateTo;
}
