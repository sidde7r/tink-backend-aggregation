package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditBalancesEntity {

    private String associatedFullDepositAccountKey;
    private BigDecimal authorizations;
    private BigDecimal availableCredit;
    private BigDecimal bookBalance;
    private BigDecimal cashAdvanceAvailableAmount;
    private String controlAccountNumber;
    private BigDecimal creditLimit;
    private String currency;
    private BigDecimal usedCredit;

    public BigDecimal getAvailableCredit() {
        return availableCredit;
    }

    public String getCurrency() {
        return currency;
    }
}
