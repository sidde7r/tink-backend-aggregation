package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class OpBankCardEntity {
    private String cardNumber;
    private String cardNumberMasked;
    private String name;
    private double reservations;
    private String creditAccountNumber;
    private double balance;
    private double liquidFunds;
    private double creditLimit;
    private double purchaseLimit;
    private double withdrawalLimit;
    private double internetPaymentLimit;
    private String expiryDate;
    private String debitAccountNumber;
    private String statusCode;
    private String accountNumber;
    private String solidarityCode;
    private String parallelUseCode;
    private String warningCode;
    private String productCode;
    private int stateCode;
    private String newestTransactionId;
    private String referenceNumber;
    private boolean plussaFeature;
    private String plussaNumber;
    private String internetPaymentCode;
    private boolean internetPaymentIncluded;
    private boolean isCreditCard;
    private String appearance;
    private boolean isDebitCard;
    private boolean isMasterCard;
    private boolean isVisa;
    private boolean isVisaDebitCard;
    private boolean isVisaCreditCard;
    private boolean isLimitsFeatureSupported;
    private boolean isDebitLimitsFeatureSupported;
    private boolean isCreditLimitsFeatureSupported;
    private int priority;
    private boolean isVisible;
    private boolean isPinCodeOrderAllowed;

    @JsonIgnore
    public CreditCardAccount toTinkCardAccount() {
        return CreditCardAccount.builder(creditAccountNumber, Amount.inEUR(balance), Amount.inEUR(liquidFunds))
                .setAccountNumber(cardNumberMasked)
                .setName(name)
                .setBankIdentifier(cardNumber)
                .build();
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardNumberMasked() {
        return cardNumberMasked;
    }

    public String getName() {
        return name;
    }

    public double getReservations() {
        return reservations;
    }

    public String getCreditAccountNumber() {
        return creditAccountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public double getLiquidFunds() {
        return liquidFunds;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public double getPurchaseLimit() {
        return purchaseLimit;
    }

    public double getWithdrawalLimit() {
        return withdrawalLimit;
    }

    public double getInternetPaymentLimit() {
        return internetPaymentLimit;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getDebitAccountNumber() {
        return debitAccountNumber;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getSolidarityCode() {
        return solidarityCode;
    }

    public String getParallelUseCode() {
        return parallelUseCode;
    }

    public String getWarningCode() {
        return warningCode;
    }

    public String getProductCode() {
        return productCode;
    }

    public int getStateCode() {
        return stateCode;
    }

    public String getNewestTransactionId() {
        return newestTransactionId;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public boolean isPlussaFeature() {
        return plussaFeature;
    }

    public String getPlussaNumber() {
        return plussaNumber;
    }

    public String getInternetPaymentCode() {
        return internetPaymentCode;
    }

    public boolean isInternetPaymentIncluded() {
        return internetPaymentIncluded;
    }

    public boolean isCreditCard() {
        return isCreditCard;
    }

    public String getAppearance() {
        return appearance;
    }

    public boolean isDebitCard() {
        return isDebitCard;
    }

    public boolean isMasterCard() {
        return isMasterCard;
    }

    public boolean isVisa() {
        return isVisa;
    }

    public boolean isVisaDebitCard() {
        return isVisaDebitCard;
    }

    public boolean isVisaCreditCard() {
        return isVisaCreditCard;
    }

    public boolean isLimitsFeatureSupported() {
        return isLimitsFeatureSupported;
    }

    public boolean isDebitLimitsFeatureSupported() {
        return isDebitLimitsFeatureSupported;
    }

    public boolean isCreditLimitsFeatureSupported() {
        return isCreditLimitsFeatureSupported;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public boolean isPinCodeOrderAllowed() {
        return isPinCodeOrderAllowed;
    }
}
