package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.loan.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoansEntity {
    private String shortName;
    private String monthlyPaymentBeforeTax;
    private String outstandingDebt;
    private String documentId;
    private boolean inFrozenZone;
}
