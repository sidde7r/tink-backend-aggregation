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
    private Boolean accountClosed;
    private String status;
    private Boolean closed;
    private Boolean supplementaryCard;
    private Boolean transferFromEnabled;
    private Boolean transferToEnabled;
    private Boolean paymentFromEnabled;
    private Boolean active;
    private Boolean mainCard;
    private Boolean statusNormal;
    private Boolean statusReplaced;
    private Boolean statusBlocked;
    private Boolean statusBlockedAndReplaced;
    private Boolean statusClosed;
    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    @JsonIgnore
    public CreditCardAccount toAccount() {
        return CreditCardAccount.builder(formattedNumber,
                Sparebank1AmountUtils.constructAmount(balanceAmountInteger, balanceAmountFraction),
                Sparebank1AmountUtils.constructAmount(creditLimitInteger, creditLimitFraction))
                .setName(name)
                .setUniqueIdentifier(id)
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

    public Boolean getAccountClosed() {
        return accountClosed;
    }

    public String getStatus() {
        return status;
    }

    public Boolean getClosed() {
        return closed;
    }

    public Boolean getSupplementaryCard() {
        return supplementaryCard;
    }

    public Boolean getTransferFromEnabled() {
        return transferFromEnabled;
    }

    public Boolean getTransferToEnabled() {
        return transferToEnabled;
    }

    public Boolean getPaymentFromEnabled() {
        return paymentFromEnabled;
    }

    public Boolean getActive() {
        return active;
    }

    public Boolean getMainCard() {
        return mainCard;
    }

    public Boolean getStatusNormal() {
        return statusNormal;
    }

    public Boolean getStatusReplaced() {
        return statusReplaced;
    }

    public Boolean getStatusBlocked() {
        return statusBlocked;
    }

    public Boolean getStatusBlockedAndReplaced() {
        return statusBlockedAndReplaced;
    }

    public Boolean getStatusClosed() {
        return statusClosed;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }
}
