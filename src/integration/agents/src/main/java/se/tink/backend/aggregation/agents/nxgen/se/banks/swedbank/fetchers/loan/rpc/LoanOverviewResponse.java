package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoanOverviewResponse {
    private List<CollateralsEntity> collaterals;
    private String mortgageCalculatorLink;
    private boolean eligibleForCarLoan;
    private LinksEntity links;
    // experimental, i.e. for logging purposes
    private List<LoanEntity> ongoingLoans;
    private List<LoanEntity> carLoans;
    private List<LoanEntity> mortgageLoanCommitments;
    private List<LoanEntity> consumptionLoans;
}
