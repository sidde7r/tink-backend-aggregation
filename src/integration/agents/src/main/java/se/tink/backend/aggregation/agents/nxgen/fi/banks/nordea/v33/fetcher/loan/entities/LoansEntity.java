package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LoansEntity {
    @Getter private String loanId;
    private String loanFormattedId;
    private String productCode;
    private String currency;
    private String group;
    private String repaymentStatus;
    private String nickname;
    private AmountEntity amount;
    private CreditEntity credit;
}
