package se.tink.backend.aggregation.agents.creditcards.coop.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.creditcards.coop.v2.AccountType;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity {
    @JsonProperty("AccountType")
    private int accountType;
    @JsonProperty("Balance")
    private Double balance;
    @JsonProperty("CreditLimit")
    private Integer creditLimit;
    @JsonProperty("CanSetInternetAndForeignPayments")
    private Boolean canMakeOnlineAndForeignPayments;
    @JsonProperty("Details")
    private List<AccountDetailsEntity> accountDetails;
    @JsonProperty("Name")
    private String accountName;
    @JsonProperty("TotalBalance")
    private Double totalBalance;

    public int getAccountType() {
        return accountType;
    }

    public AccountType getAccountTypeEnum() {
        AccountType accountType = AccountType.valueOf(this.accountType);
        if (accountType != null) {
            return accountType;
        }

        return AccountType.guessFromName(this.accountName);
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public int getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(int creditLimit) {
        this.creditLimit = creditLimit;
    }

    public boolean isCanMakeOnlineAndForeignPayments() {
        return canMakeOnlineAndForeignPayments;
    }

    public void setCanMakeOnlineAndForeignPayments(boolean canMakeOnlineAndForeignPayments) {
        this.canMakeOnlineAndForeignPayments = canMakeOnlineAndForeignPayments;
    }

    public List<AccountDetailsEntity> getAccountDetails() {
        return accountDetails;
    }

    public void setAccountDetails(
            List<AccountDetailsEntity> accountDetails) {
        this.accountDetails = accountDetails;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(Double totalBalance) {
        this.totalBalance = totalBalance;
    }

    public Account toAccount() {
        Map<String, String> accountDetailsMap = getAccountDetailsMap();
        Account account = new Account();
        account.setName(accountName);
        account.setBankId(accountDetailsMap.get("accountnumber"));
        account.setAccountNumber(accountDetailsMap.get("accountnumber"));
        account.setBalance(totalBalance);
        account.setType(guessAccountType());

        Optional<PlusGiroIdentifier> accountIdentifier = getAccountIdentifier(accountDetailsMap);
        if (accountIdentifier.isPresent()) {
            account.putIdentifier(accountIdentifier.get());
        }

        return account;
    }

    /**
     * To be able to easily do transfers to the account we can add the plusgiro identifier with OCR that should be
     * available on all coop accounts that you can pay to. There might be accounts that are special, so we only add
     * those with valid ocr and plusgiro.
     */
    private static Optional<PlusGiroIdentifier> getAccountIdentifier(Map<String, String> accountDetailsMap) {
        String plusGiro = accountDetailsMap.get("plusgironumber");
        String ocr = accountDetailsMap.get("ocrnumber");

        // Ensure we have values we expect from a valid destination account
        if (Strings.isNullOrEmpty(plusGiro) || Strings.isNullOrEmpty(ocr)) {
            return Optional.empty();
        }

        PlusGiroIdentifier plusGiroIdentifier = new PlusGiroIdentifier(plusGiro, ocr);
        if (!plusGiroIdentifier.isValid()) {
            return Optional.empty();
        } else {
            return Optional.of(plusGiroIdentifier);
        }
    }

    private Map<String, String> getAccountDetailsMap() {
        List<AccountDetailsEntity> accountDetails = getAccountDetails();
        if (accountDetails == null) {
            return Collections.emptyMap();
        }

        Map<String, String> accountDetailsMap = Maps.newHashMap();
        for (AccountDetailsEntity detailsEntity : accountDetails) {
            accountDetailsMap.put(detailsEntity.getId().toLowerCase(), detailsEntity.getValue());
        }

        return accountDetailsMap;
    }

    private AccountTypes guessAccountType() {
        AccountType type = getAccountTypeEnum();

        if (type == null) {
            return AccountTypes.OTHER;
        }

        switch (type) {
        case MEDMERA_MER:
        case MEDMERA_EFTER_1:
        case MEDMERA_EFTER_2:
        case MEDMERA_FORE:
        case MEDMERA_FAKTURA:
        case MEDMERA_VISA:
            return AccountTypes.CREDIT_CARD;
        case MEDMERA_KONTO:
        default:
            return AccountTypes.OTHER;
        }
    }
}
