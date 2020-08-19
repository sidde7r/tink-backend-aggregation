package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CollateralsEntity extends LoanEntity {
    private String description;
    private AmountEntity totalDebt;
    private List<LoanEntity> loans;
    private boolean moreCollateralsExists;

    public String getDescription() {
        return description;
    }

    public AmountEntity getTotalDebt() {
        return totalDebt;
    }

    public List<LoanEntity> getLoans() {
        return loans;
    }

    public boolean isMoreCollateralsExists() {
        return moreCollateralsExists;
    }
}
