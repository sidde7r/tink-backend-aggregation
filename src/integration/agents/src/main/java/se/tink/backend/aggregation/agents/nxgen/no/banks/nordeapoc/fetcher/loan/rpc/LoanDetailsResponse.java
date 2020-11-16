package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.loan.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.loan.entity.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.loan.entity.CreditEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.loan.entity.InterestEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.loan.entity.OwnersEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class LoanDetailsResponse {

    private static final TypeMapper<LoanDetails.Type> LOAN_TYPE_MAPPER =
            TypeMapper.<LoanDetails.Type>builder()
                    .put(LoanDetails.Type.MORTGAGE, "mortgage")
                    .put(LoanDetails.Type.CREDIT, "credit_loan")
                    .put(LoanDetails.Type.OTHER, "other")
                    .build();

    private String currency;
    private String group;
    private InterestEntity interest;
    private AmountEntity amount;
    private CreditEntity credit;
    private List<OwnersEntity> owners;
    private String nickname;
    private String loanId;
    private String loanFormattedId;
    private String productCode;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private Date firstDrawDownDate;

    public LoanDetails.Type getTinkLoanType() {
        return LOAN_TYPE_MAPPER.translate(group).orElse(LoanDetails.Type.OTHER);
    }
}
