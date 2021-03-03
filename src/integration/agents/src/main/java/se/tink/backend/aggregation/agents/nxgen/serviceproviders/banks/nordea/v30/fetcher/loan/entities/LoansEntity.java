package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class LoansEntity {
    private String loanId;
    private String loanFormattedId;
    private String productCode;
    private String currency;
    private String group;
    private String repaymentStatus;
    private String nickname;
    private AmountEntity amount;
    private CreditEntity credit;
}
