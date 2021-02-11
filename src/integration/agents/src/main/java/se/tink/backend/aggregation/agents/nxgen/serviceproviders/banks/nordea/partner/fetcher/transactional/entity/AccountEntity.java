package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class AccountEntity {
    /// The unique ID of the account (required)
    private String accountId;
    private String mainAccountNumber;
    private String displayAccountNumber;
    private String iban;
    private String bic;
    private String registrationNumber;
    private String countryCode;
    private String productCode;
    private String productName;
    private String nickname;
    private String productType;
    private String category;
    private String accountStatus;
    private BigDecimal bookedBalance;
    private BigDecimal creditLimit;
    private BigDecimal availableBalance;
    private BigDecimal equivalentBalance;
    private String currency;
    private String equivalentCurrency;
    private int remainingFreeWithdrawals;
    private String latestTransactionDate;
    private String statementFormat;
    private String maturityDueDate;
    private boolean coveredByDepositGuarantee;
    /// List of users and their roles linked to the account (required)
    private List<RolesEntity> roles;
    /// List of permissions the user has on the account (required)
    private PermissionsEntity permissions;
    private InterestInfoEntity interestInfo;
    private FlexiDepositEntity flexiDeposit;
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
}
