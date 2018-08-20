package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1AmountUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

@JsonObject
public class CreditCardAccountEntity {
    private String id;
    private String encryptedId;
    private String name;
    private String formattedNumber;
    private String cardType;
    private String formattedNumberForPlacementOnCard;
    private String expiryDate;
    private String statusCode;
    private HashMap<String, Boolean> allowedActions;
    private String balanceAmountInteger;
    private String balanceAmountFraction;
    private String creditLimitInteger;
    private String creditLimitFraction;
    private boolean accountClosed;
    private String status;
    private boolean supplementaryCard;
    private boolean transferFromEnabled;
    private boolean transferToEnabled;
    private boolean paymentFromEnabled;
    private boolean active;
    private boolean mainCard;
    private boolean statusNormal;
    private boolean statusReplaced;
    private boolean statusBlocked;
    private boolean statusBlockedAndReplaced;
    private boolean statusClosed;
    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    @JsonIgnore
    public CreditCardAccount toAccount() {
        return CreditCardAccount.builder(formattedNumber,
                Sparebank1AmountUtils.constructAmount(balanceAmountInteger, balanceAmountFraction),
                Sparebank1AmountUtils.constructAmount(creditLimitInteger, creditLimitFraction))
                .setAccountNumber(formattedNumber)
                .setName(name)
                .setBankIdentifier(id)
                .build();
    }

    public String getId() {
        return id;
    }

    public String getEncryptedId() {
        return encryptedId;
    }

    public String getName() {
        return name;
    }

    public String getFormattedNumber() {
        return formattedNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public String getFormattedNumberForPlacementOnCard() {
        return formattedNumberForPlacementOnCard;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public HashMap<String, Boolean> getAllowedActions() {
        return allowedActions;
    }

    public String getBalanceAmountInteger() {
        return balanceAmountInteger;
    }

    public String getBalanceAmountFraction() {
        return balanceAmountFraction;
    }

    public String getCreditLimitInteger() {
        return creditLimitInteger;
    }

    public String getCreditLimitFraction() {
        return creditLimitFraction;
    }

    public boolean isAccountClosed() {
        return accountClosed;
    }

    public String getStatus() {
        return status;
    }

    public boolean getSupplementaryCard() {
        return supplementaryCard;
    }

    public boolean getTransferFromEnabled() {
        return transferFromEnabled;
    }

    public boolean getTransferToEnabled() {
        return transferToEnabled;
    }

    public boolean getPaymentFromEnabled() {
        return paymentFromEnabled;
    }

    public boolean getActive() {
        return active;
    }

    public boolean getMainCard() {
        return mainCard;
    }

    public boolean getStatusNormal() {
        return statusNormal;
    }

    public boolean getStatusReplaced() {
        return statusReplaced;
    }

    public boolean getStatusBlocked() {
        return statusBlocked;
    }

    public boolean getStatusBlockedAndReplaced() {
        return statusBlockedAndReplaced;
    }

    public boolean getStatusClosed() {
        return statusClosed;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }
}
