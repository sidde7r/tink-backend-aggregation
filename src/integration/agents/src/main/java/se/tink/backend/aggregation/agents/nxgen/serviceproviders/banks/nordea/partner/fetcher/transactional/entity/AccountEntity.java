package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("main_account_number")
    private String mainAccountNumber;

    @JsonProperty("display_account_number")
    private String displayAccountNumber;

    private String iban;
    private String bic;

    @JsonProperty("registration_number")
    private String registrationNumber;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty("product_name")
    private String productName;

    private String nickname;

    @JsonProperty("product_type")
    private String productType;

    private String category;

    @JsonProperty("account_status")
    private String accountStatus;

    @JsonProperty("booked_balance")
    private BigDecimal bookedBalance;

    @JsonProperty("credit_limit")
    private BigDecimal creditLimit;

    @JsonProperty("available_balance")
    private BigDecimal availableBalance;

    @JsonProperty("equivalent_balance")
    private BigDecimal equivalentBalance;

    private String currency;

    @JsonProperty("equivalent_currency")
    private String equivalentCurrency;

    @JsonProperty("remaining_free_withdrawals")
    private int remainingFreeWithdrawals;

    @JsonProperty("latest_transaction_date")
    private String latestTransactionDate;

    @JsonProperty("statement_format")
    private String statementFormat;

    @JsonProperty("maturity_due_date")
    private String maturityDueDate;

    @JsonProperty("covered_by_deposit_guarantee")
    private boolean coveredByDepositGuarantee;

    private List<RolesEntity> roles;
    private PermissionsEntity permissions;

    @JsonProperty("interest_info")
    private InterestInfoEntity interestInfo;

    @JsonProperty("flexi_deposit")
    private FlexiDepositEntity flexiDeposit;

    @JsonProperty("transaction_list_search_criteria")
    private TransactionListSearchCriteriaEntity transactionListSearchCriteria;

    @JsonIgnore
    public String getHolderName() {
        return Optional.ofNullable(roles).orElse(Collections.emptyList()).stream()
                .filter(RolesEntity::isOwner)
                .map(RolesEntity::getName)
                .findFirst()
                .orElse(null);
    }

    @JsonIgnore
    public boolean hasIban() {
        return !Strings.isNullOrEmpty(iban);
    }

    public String getAccountId() {
        return accountId;
    }

    public String getMainAccountNumber() {
        return mainAccountNumber;
    }

    public String getDisplayAccountNumber() {
        return displayAccountNumber;
    }

    public String getIban() {
        return iban;
    }

    public String getBic() {
        return bic;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProductType() {
        return productType;
    }

    public String getCategory() {
        return category;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public BigDecimal getBookedBalance() {
        return bookedBalance;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getEquivalentBalance() {
        return equivalentBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public String getEquivalentCurrency() {
        return equivalentCurrency;
    }

    public int getRemainingFreeWithdrawals() {
        return remainingFreeWithdrawals;
    }

    public String getLatestTransactionDate() {
        return latestTransactionDate;
    }

    public String getStatementFormat() {
        return statementFormat;
    }

    public String getMaturityDueDate() {
        return maturityDueDate;
    }

    public boolean isCoveredByDepositGuarantee() {
        return coveredByDepositGuarantee;
    }

    public List<RolesEntity> getRoles() {
        return roles;
    }

    public PermissionsEntity getPermissions() {
        return permissions;
    }

    public InterestInfoEntity getInterestInfo() {
        return interestInfo;
    }

    public FlexiDepositEntity getFlexiDeposit() {
        return flexiDeposit;
    }

    public TransactionListSearchCriteriaEntity getTransactionListSearchCriteria() {
        return transactionListSearchCriteria;
    }
}
