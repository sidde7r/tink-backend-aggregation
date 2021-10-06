package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.loan.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoanStatusEntity {
    private String dateOfStatus;
    private String outstandingDebt;
    private String remainingTerm;
}
