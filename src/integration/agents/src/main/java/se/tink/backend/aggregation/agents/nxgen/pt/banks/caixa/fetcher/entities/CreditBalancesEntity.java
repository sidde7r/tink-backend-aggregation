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

    public String getAssociatedFullDepositAccountKey() {
        return associatedFullDepositAccountKey;
    }

    public BigDecimal getAuthorizations() {
        return authorizations;
    }

    public BigDecimal getAvailableCredit() {
        return availableCredit;
    }

    public BigDecimal getBookBalance() {
        return bookBalance;
    }

    public BigDecimal getCashAdvanceAvailableAmount() {
        return cashAdvanceAvailableAmount;
    }

    public String getControlAccountNumber() {
        return controlAccountNumber;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getUsedCredit() {
        return usedCredit;
    }
}
