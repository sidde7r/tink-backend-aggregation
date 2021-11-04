package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.InterestSpecificationEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.LoanDetailsAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.LoanInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.UpcomingInvoiceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class DetailedLoanResponse {

    @JsonProperty("loanDetail")
    private LoanDetailsAccountEntity loanDetails;

    private UpcomingInvoiceEntity upcomingInvoice;
    private LinkEntity links;
    private String loanLender;
    private List<InterestSpecificationEntity> interestSpecifications;
    private AmountEntity debt;
    private LoanInfoEntity loan;
}
