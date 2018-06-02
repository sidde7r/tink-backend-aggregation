package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.core.Amount;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEncryptedId() {
        return encryptedId;
    }

    public void setEncryptedId(String encryptedId) {
        this.encryptedId = encryptedId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormattedNumber() {
        return formattedNumber;
    }

    public void setFormattedNumber(String formattedNumber) {
        this.formattedNumber = formattedNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getFormattedNumberForPlacementOnCard() {
        return formattedNumberForPlacementOnCard;
    }

    public void setFormattedNumberForPlacementOnCard(String formattedNumberForPlacementOnCard) {
        this.formattedNumberForPlacementOnCard = formattedNumberForPlacementOnCard;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public HashMap<String, Boolean> getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(HashMap<String, Boolean> allowedActions) {
        this.allowedActions = allowedActions;
    }

    public String getBalanceAmountInteger() {
        return balanceAmountInteger;
    }

    public void setBalanceAmountInteger(String balanceAmountInteger) {
        this.balanceAmountInteger = balanceAmountInteger;
    }

    public String getBalanceAmountFraction() {
        return balanceAmountFraction;
    }

    public void setBalanceAmountFraction(String balanceAmountFraction) {
        this.balanceAmountFraction = balanceAmountFraction;
    }

    public String getCreditLimitInteger() {
        return creditLimitInteger;
    }

    public void setCreditLimitInteger(String creditLimitInteger) {
        this.creditLimitInteger = creditLimitInteger;
    }

    public String getCreditLimitFraction() {
        return creditLimitFraction;
    }

    public void setCreditLimitFraction(String creditLimitFraction) {
        this.creditLimitFraction = creditLimitFraction;
    }

    public Boolean getAccountClosed() {
        return accountClosed;
    }

    public void setAccountClosed(Boolean accountClosed) {
        this.accountClosed = accountClosed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    public Boolean getSupplementaryCard() {
        return supplementaryCard;
    }

    public void setSupplementaryCard(Boolean supplementaryCard) {
        this.supplementaryCard = supplementaryCard;
    }

    public Boolean getTransferFromEnabled() {
        return transferFromEnabled;
    }

    public void setTransferFromEnabled(Boolean transferFromEnabled) {
        this.transferFromEnabled = transferFromEnabled;
    }

    public Boolean getTransferToEnabled() {
        return transferToEnabled;
    }

    public void setTransferToEnabled(Boolean transferToEnabled) {
        this.transferToEnabled = transferToEnabled;
    }

    public Boolean getPaymentFromEnabled() {
        return paymentFromEnabled;
    }

    public void setPaymentFromEnabled(Boolean paymentFromEnabled) {
        this.paymentFromEnabled = paymentFromEnabled;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getMainCard() {
        return mainCard;
    }

    public void setMainCard(Boolean mainCard) {
        this.mainCard = mainCard;
    }

    public Boolean getStatusNormal() {
        return statusNormal;
    }

    public void setStatusNormal(Boolean statusNormal) {
        this.statusNormal = statusNormal;
    }

    public Boolean getStatusReplaced() {
        return statusReplaced;
    }

    public void setStatusReplaced(Boolean statusReplaced) {
        this.statusReplaced = statusReplaced;
    }

    public Boolean getStatusBlocked() {
        return statusBlocked;
    }

    public void setStatusBlocked(Boolean statusBlocked) {
        this.statusBlocked = statusBlocked;
    }

    public Boolean getStatusBlockedAndReplaced() {
        return statusBlockedAndReplaced;
    }

    public void setStatusBlockedAndReplaced(Boolean statusBlockedAndReplaced) {
        this.statusBlockedAndReplaced = statusBlockedAndReplaced;
    }

    public Boolean getStatusClosed() {
        return statusClosed;
    }

    public void setStatusClosed(Boolean statusClosed) {
        this.statusClosed = statusClosed;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    public void setLinks(
            HashMap<String, LinkEntity> links) {
        this.links = links;
    }

    private Amount getBalance() {
        return Amount.inNOK(StringUtils.parseAmount(balanceAmountInteger + "," + balanceAmountFraction));
    }

    private Amount getAvailableCredit() {
        return Amount.inNOK(StringUtils.parseAmount(creditLimitInteger + "," + creditLimitFraction));
    }

    public CreditCardAccount toAccount() {
        return CreditCardAccount.builder(formattedNumber, getBalance(), getAvailableCredit())
                .setName(name)
                .setUniqueIdentifier(id)
                .build();
    }
}
