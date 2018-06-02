package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanOverviewResponse {
    private List<CollateralsEntity> collaterals;
    private String mortgageCalculatorLink;
    private boolean eligibleForCarLoan;
    private LinksEntity links;

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
}
