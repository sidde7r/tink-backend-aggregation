package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities.FinancedObjectEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities.InterestEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities.OwnersEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LoanDetailsResponse {

    private static final TypeMapper<LoanDetails.Type> LOAN_TYPE_MAPPER =
            TypeMapper.<LoanDetails.Type>builder()
                    .put(LoanDetails.Type.MORTGAGE, "mortgage")
                    .put(LoanDetails.Type.OTHER, "other")
                    .build();

    private String loanId;
    private String loanFormattedId;
    private String productCode;
    private String currency;
    private String group;

    private InterestEntity interest;
    private AmountEntity amount;
    private List<OwnersEntity> owners;

    private String nickname;

    private FinancedObjectEntity financedObjectEntity;

    public LoanDetails.Type getTinkLoanType() {
        return LOAN_TYPE_MAPPER.translate(group).orElse(LoanDetails.Type.OTHER);
    }
}
