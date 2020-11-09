package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.Getter;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class AccountEntity {

    private static final String OWNER_ROLE = "owner";
    private static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "transaction")
                    .put(AccountTypes.SAVINGS, "savings")
                    .put(AccountTypes.CREDIT_CARD, "credit", "combined")
                    .put(AccountTypes.LOAN, "mortgage")
                    .build();

    private String accountId;
    private String bic;
    private String iban;
    private String nickname;
    private String displayAccountNumber;
    private String productCode;
    private String productName;
    private String category;
    private String currency;
    private BigDecimal bookedBalance;
    private BigDecimal availableBalance;
    private BigDecimal creditLimit;
    private PermissionsEntity permissions;
    private List<RoleEntity> roles;

    public Optional<AccountTypes> getTinkAccountType() {
        return ACCOUNT_TYPE_MAPPER.translate(category);
    }

    public AccountCapabilities.Answer canPlaceFunds() {
        return checkPermission(() -> permissions.getCanDepositToAccount());
    }

    public AccountCapabilities.Answer canWithdrawCash() {
        return checkPermission(() -> permissions.getCanTransferFromAccount());
    }

    public AccountCapabilities.Answer canExecuteExternalTransfer() {
        return checkPermission(() -> permissions.getCanPayFromAccount());
    }

    public AccountCapabilities.Answer canReceiveExternalTransfer() {
        return checkPermission(() -> permissions.getCanTransferToAccount());
    }

    private AccountCapabilities.Answer checkPermission(Supplier<Boolean> supplier) {
        return permissions == null
                ? AccountCapabilities.Answer.UNKNOWN
                : AccountCapabilities.Answer.From(supplier.get());
    }

    public ExactCurrencyAmount getBookedBalance() {
        return ExactCurrencyAmount.of(bookedBalance, currency);
    }

    public ExactCurrencyAmount getAvailableBalance() {
        return ExactCurrencyAmount.of(availableBalance, currency);
    }

    public ExactCurrencyAmount getCreditLimit() {
        return ExactCurrencyAmount.of(creditLimit, currency);
    }

    public String getOwner() {
        return roles.stream()
                .filter(x -> OWNER_ROLE.equalsIgnoreCase(x.getRole()))
                .findFirst()
                .map(RoleEntity::getName)
                .orElse(null);
    }
}
