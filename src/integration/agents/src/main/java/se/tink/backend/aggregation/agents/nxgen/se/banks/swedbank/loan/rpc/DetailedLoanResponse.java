package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.InterestSpecificationEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanDetailsAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.UpcomingInvoiceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailedLoanResponse {

    private LoanDetailsAccountEntity loanDetail;
    private UpcomingInvoiceEntity upcomingInvoice;
    private LinkEntity links;
    private String loanLender;
    private List<InterestSpecificationEntity> interestSpecifications;
    private AmountEntity debt;
    private LoanInfoEntity loan;

    public LoanInfoEntity getLoan() {
        return loan;
    }

    public LoanDetailsAccountEntity getLoanDetails() {
        return loanDetail;
    }

    public UpcomingInvoiceEntity getUpcomingInvoice() {
        return upcomingInvoice;
    }

    public LinkEntity getLinks() {
        return links;
    }

    public String getLoanLender() {
        return loanLender;
    }

    public List<InterestSpecificationEntity> getInterestSpecifications() {
        return interestSpecifications;
    }

    public AmountEntity getDebt() {
        return debt;
    }
}
