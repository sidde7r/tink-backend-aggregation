package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import java.math.BigDecimal;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardEntity {

    private Boolean active;
    private Boolean cancelled;
    private String cardAccountCurrency;
    private String cardAccountDescription;
    private String cardAccountId;
    private String cardAccountNumber;
    private String cardAlias;
    private String cardKey;
    private String cardNumberServiceMBWAY;
    private String depositAccount;
    private String description;
    private String expirationDate;
    private String expirationDateMessage;
    private String maskedCardNumber;
    private Boolean mbNetIndicator;
    private String mobileNumberMBWAY;
    private Integer mobilePrefixMBWAY;
    private Boolean prePaidCard;
    private Boolean prePaidDualCreditCard;
    private String printedName;
    private String serviceIdentifierNumberMBWAY;
    private BigDecimal totalOutstandingBalance;
    private List<CardTransactionEntity> transactions;

    public Boolean getActive() {
        return active;
    }

    public Boolean getCancelled() {
        return cancelled;
    }

    public String getCardAccountCurrency() {
        return cardAccountCurrency;
    }

    public String getCardAccountDescription() {
        return cardAccountDescription;
    }

    public String getCardAccountId() {
        return cardAccountId;
    }

    public String getCardAccountNumber() {
        return cardAccountNumber;
    }

    public String getCardAlias() {
        return cardAlias;
    }

    public String getCardKey() {
        return cardKey;
    }

    public String getCardNumberServiceMBWAY() {
        return cardNumberServiceMBWAY;
    }

    public String getDepositAccount() {
        return depositAccount;
    }

    public String getDescription() {
        return description;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getExpirationDateMessage() {
        return expirationDateMessage;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public Boolean getMbNetIndicator() {
        return mbNetIndicator;
    }

    public String getMobileNumberMBWAY() {
        return mobileNumberMBWAY;
    }

    public Integer getMobilePrefixMBWAY() {
        return mobilePrefixMBWAY;
    }

    public Boolean getPrePaidCard() {
        return prePaidCard;
    }

    public Boolean getPrePaidDualCreditCard() {
        return prePaidDualCreditCard;
    }

    public String getPrintedName() {
        return printedName;
    }

    public String getServiceIdentifierNumberMBWAY() {
        return serviceIdentifierNumberMBWAY;
    }

    public BigDecimal getTotalOutstandingBalance() {
        return totalOutstandingBalance;
    }

    public List<CardTransactionEntity> getTransactions() {
        return transactions;
    }
}
