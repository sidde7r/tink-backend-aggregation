package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan.entities.LoanStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan.entities.MoreDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan.entities.NextPaymentEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class MortgageDetailsResponse {
    private String creditor;
    private NextPaymentEntity nextPayment;
    private LoanStatusEntity loanStatus;
    private MoreDetailsEntity moreDetails;
}
