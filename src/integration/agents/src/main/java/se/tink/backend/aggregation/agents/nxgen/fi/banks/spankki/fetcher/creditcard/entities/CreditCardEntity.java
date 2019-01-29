package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.creditcard.entities;



import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CreditCardEntity {
    
    public String amount;
    public String available;
    public String balance;
    public String cardStatus;
    public String contractNr;
    public String credit;
    public String creditAccountNr;
    public String creditName;
    public String interestRate;
    public boolean isCredit;
    public String minPaymentPercent;
    public boolean movable;
    public String paymentDate;
    public boolean pinReorderOK;
    public String productCode;
    public String totalLimit;

    public String getAmount() {
        return amount;
    }

    public String getAvailable() {
        return available;
    }

    public String getCardStatus() {
        return cardStatus;
    }

    public String getContractNr() {
        return contractNr;
    }

    public String getCredit() {
        return credit;
    }

    public String getCreditAccountNr() {
        return creditAccountNr;
    }

    public String getCreditName() {
        return creditName;
    }

    public String getInterestRate() {
        return interestRate;
    }

    public boolean isCredit() {
        return isCredit;
    }

    public String getMinPaymentPercent() {
        return minPaymentPercent;
    }

    public boolean isMovable() {
        return movable;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public boolean isPinReorderOK() {
        return pinReorderOK;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getTotalLimit() {
        return totalLimit;
    }

    public Amount toTinkAmount(String m){
        return Amount.inEUR(Double.parseDouble(m));
    }

    public CreditCardAccount toTinkAccount() {
        return CreditCardAccount.builder(this.getCreditAccountNr(), toTinkAmount(this.balance), toTinkAmount(this.totalLimit))
                .setAccountNumber(this.creditAccountNr)
                .setBankIdentifier(this.getCreditAccountNr())
                .setName(this.creditName)
                .build();
    }

}
