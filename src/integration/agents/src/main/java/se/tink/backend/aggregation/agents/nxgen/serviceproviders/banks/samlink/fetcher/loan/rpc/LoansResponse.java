package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.loan.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc.LinksResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoansResponse extends LinksResponse {
    private Boolean notAllLoansShown;
    private AmountEntity totalBalance;
    private List<LoanEntity> loans;

    public Boolean getNotAllLoansShown() {
        return notAllLoansShown;
    }

    public AmountEntity getTotalBalance() {
        return totalBalance;
    }

    public List<LoanEntity> getLoans() {
        return loans;
    }
}
