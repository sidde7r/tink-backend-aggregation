package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CollateralsEntity {
    private String description;
    private List<LoanEntity> loans;
    private boolean moreCollateralsExists;
    private AmountEntity totalDebt;

    public String getDescription() {
        return description;
    }

    public List<LoanEntity> getLoans() {
        return loans;
    }

    public boolean isMoreCollateralsExists() {
        return moreCollateralsExists;
    }

    public AmountEntity getTotalDebt() {
        return totalDebt;
    }
}
