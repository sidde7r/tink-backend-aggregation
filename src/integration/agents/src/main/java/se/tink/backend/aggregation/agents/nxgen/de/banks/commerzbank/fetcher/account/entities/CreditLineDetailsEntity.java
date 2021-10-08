package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CreditLineDetailsEntity {
    private BalanceEntity creditLine;
    private String creditType;
    private String interestRateExistingCreditLine;
    private String interestRateToleratedOverdraft;
    private String validUntil;
}
