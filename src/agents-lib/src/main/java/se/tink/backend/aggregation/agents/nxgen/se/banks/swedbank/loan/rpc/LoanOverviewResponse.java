package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.CollateralsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanOverviewResponse {
    private List<CollateralsEntity> collaterals;
    private String mortgageCalculatorLink;
    private boolean eligibleForCarLoan;
    private LinksEntity links;
    private List<LoanEntity> ongoingLoans;
    private List<LoanEntity> carLoans;
    private List<LoanEntity> mortgageLoanCommitments;
    private List<LoanEntity> consumptionLoans;

    public List<CollateralsEntity> getCollaterals() {
        return collaterals;
    }

    public String getMortgageCalculatorLink() {
        return mortgageCalculatorLink;
    }

    public boolean isEligibleForCarLoan() {
        return eligibleForCarLoan;
    }

    public LinksEntity getLinks() {
        return links;
    }

    // experimental, i.e. for logging purposes
    public List<LoanEntity> getOngoingLoans() {
        return ongoingLoans;
    }

    public List<LoanEntity> getCarLoans() {
        return carLoans;
    }

    public List<LoanEntity> getMortgageLoanCommitments() {
        return mortgageLoanCommitments;
    }

    public List<LoanEntity> getConsumptionLoans() {
        return consumptionLoans;
    }
}
