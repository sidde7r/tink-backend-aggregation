package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty private String accountId;
    @JsonProperty private String mainAccountNumber;
    @JsonProperty private String displayAccountNumber;
    @JsonProperty private String iban;
    @JsonProperty private String bic;
    @JsonProperty private String registrationNumber;
    @JsonProperty private String countryCode;
    @JsonProperty private String productCode;
    @JsonProperty private String productName;
    @JsonProperty private String nickname;
    @JsonProperty private String productType;
    @JsonProperty private String category;
    @JsonProperty private String accountStatus;
    @JsonProperty private BigDecimal bookedBalance;
    @JsonProperty private BigDecimal creditLimit;
    @JsonProperty private BigDecimal availableBalance;
    @JsonProperty private BigDecimal equivalentBalance;
    @JsonProperty private String currency;
    @JsonProperty private String equivalentCurrency;
    @JsonProperty private int remainingFreeWithdrawals;
    @JsonProperty private String latestTransactionDate;
    @JsonProperty private String statementFormat;
    @JsonProperty private String maturityDueDate;
    @JsonProperty private boolean coveredByDepositGuarantee;
    /// List of users and their roles linked to the account (required)
    @JsonProperty private List<RolesEntity> roles;
    /// List of permissions the user has on the account (required)
    @JsonProperty private PermissionsEntity permissions;
    @JsonProperty private InterestInfoEntity interestInfo;
    @JsonProperty private FlexiDepositEntity flexiDeposit;
    @JsonProperty private TransactionListSearchCriteriaEntity transactionListSearchCriteria;

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
