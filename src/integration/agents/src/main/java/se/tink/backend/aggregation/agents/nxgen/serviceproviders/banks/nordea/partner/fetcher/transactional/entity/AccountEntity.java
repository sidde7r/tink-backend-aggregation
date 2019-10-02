package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

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
    private double bookedBalance;

    @JsonProperty("credit_limit")
    private double creditLimit;

    @JsonProperty("available_balance")
    private double availableBalance;

    @JsonProperty("equivalent_balance")
    private double equivalentBalance;

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

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(NordeaPartnerConstants.ACCOUNT_TYPE_MAPPER, category)
                .withBalance(
                        BalanceModule.of(
                                ExactCurrencyAmount.of(
                                        BigDecimal.valueOf(availableBalance), currency)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(displayAccountNumber)
                                .withAccountName(nickname)
                                .addIdentifier(AccountIdentifier.create(Type.IBAN, iban))
                                .setProductName(productName)
                                .build())
                .addHolderName(getHolderName())
                .setApiIdentifier(accountId)
                .build();
    }

    @JsonIgnore
    private String getHolderName() {
        return Optional.ofNullable(roles).orElse(Collections.emptyList()).stream()
                .filter(RolesEntity::isOwner)
                .map(RolesEntity::getName)
                .findFirst()
                .orElse(null);
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return NordeaPartnerConstants.ACCOUNT_TYPE_MAPPER.isOneOf(
                category, EnumSet.of(AccountTypes.CHECKING, AccountTypes.SAVINGS));
    }
}
