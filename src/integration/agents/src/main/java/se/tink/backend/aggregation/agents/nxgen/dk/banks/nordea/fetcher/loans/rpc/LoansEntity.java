package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LoansEntity {
    private String loanId;

    private String loanFormattedId;

    private String productCode;

    private String currency;
    private String group;

    private String repaymentStatus;

    private String nickname;
    private InterestEntity interest;
    private AmountEntity amount;

    private FollowingPaymentEntity followingPayment;

    private RepaymentScheduleEntity repaymentSchedule;

    private FinancedObjectEntity financedObject;

    private List<OwnersEntity> owners;

    private String branchId;
}
