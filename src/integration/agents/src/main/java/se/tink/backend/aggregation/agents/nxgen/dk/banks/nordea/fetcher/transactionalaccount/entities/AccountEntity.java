package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AccountEntity {

    @JsonIgnore private final BalanceHelper balanceHelper = new BalanceHelper();

    private String accountId;
    private String iban;
    private String nickname;
    private String displayAccountNumber;
    private String productCode;
    private String productName;
    private String category;
    private Double bookedBalance;
    private Double availableBalance;
    private Double creditLimit;
    private String currency;
    private PermissionsEntity permissions;

    public Optional<TransactionalAccount> toTinkAccount() {
        BalanceModule balanceModule =
                BalanceModule.builder()
                        .withBalance(balanceHelper.getExactBalance())
                        .setAvailableCredit(balanceHelper.getAvailableCredit())
                        .setAvailableBalance(balanceHelper.calculateAvailableBalance())
                        .setCreditLimit(balanceHelper.getCreditLimit())
                        .build();
        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(displayAccountNumber)
                        .withAccountNumber(iban)
                        .withAccountName(nickname)
                        .addIdentifier(AccountIdentifier.create(AccountIdentifierType.IBAN, iban))
                        .build();
        TransactionalAccountType accountType = getTinkAccountType();
        return TransactionalAccount.nxBuilder()
                .withType(accountType)
                .withInferredAccountFlags()
                .withBalance(balanceModule)
                .withId(idModule)
                .canPlaceFunds(canPlaceFunds())
                .canWithdrawCash(canWithdrawCash())
                .canExecuteExternalTransfer(canExecuteExternalTransfer())
                .canReceiveExternalTransfer(canReceiveExternalTransfer())
                .setApiIdentifier(accountId)
                .putInTemporaryStorage(NordeaDkConstants.StorageKeys.PRODUCT_CODE, productCode)
                .build();
    }

    @JsonIgnore
    private TransactionalAccountType getTinkAccountType() {
        return TransactionalAccountType.from(
                        NordeaDkConstants.ACCOUNT_TYPE_MAPPER
                                .translate(category)
                                .orElse(AccountTypes.OTHER))
                .orElse(TransactionalAccountType.OTHER);
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        switch (getTinkAccountType()) {
            case CHECKING:
            case OTHER:
            case SAVINGS:
                return true;
            default:
                return false;
        }
    }

    @JsonIgnore
    private AccountCapabilities.Answer canPlaceFunds() {
        if (Objects.isNull(permissions)) {
            return AccountCapabilities.Answer.UNKNOWN;
        }

        return AccountCapabilities.Answer.From(permissions.getCanDepositToAccount());
    }

    @JsonIgnore
    private AccountCapabilities.Answer canWithdrawCash() {
        if (Objects.isNull(permissions)) {
            return AccountCapabilities.Answer.UNKNOWN;
        }

        return AccountCapabilities.Answer.From(permissions.getCanTransferFromAccount());
    }

    @JsonIgnore
    private AccountCapabilities.Answer canExecuteExternalTransfer() {
        if (Objects.isNull(permissions)) {
            return AccountCapabilities.Answer.UNKNOWN;
        }

        return AccountCapabilities.Answer.From(permissions.getCanPayFromAccount());
    }

    @JsonIgnore
    private AccountCapabilities.Answer canReceiveExternalTransfer() {
        if (Objects.isNull(permissions)) {
            return AccountCapabilities.Answer.UNKNOWN;
        }

        return AccountCapabilities.Answer.From(permissions.getCanTransferToAccount());
    }

    private class BalanceHelper {
        private ExactCurrencyAmount getExactBalance() {
            return isCreditAccount() ? getBookedBalance() : tryAvailableBalanceOrBooked();
        }

        private boolean isCreditAccount() {
            return getCreditLimit().getExactValue().compareTo(BigDecimal.ZERO) > 0;
        }

        private ExactCurrencyAmount tryAvailableBalanceOrBooked() {
            return availableBalance != null ? getAvailableBalance() : getBookedBalance();
        }

        private ExactCurrencyAmount calculateAvailableBalance() {
            if (isCreditAccount()) {
                if (hasUsedAllOwnMoney()) {
                    return ExactCurrencyAmount.zero(currency);
                } else {
                    return getAvailableBalance().subtract(getCreditLimit());
                }
            }
            return tryAvailableBalanceOrBooked();
        }

        private boolean hasUsedAllOwnMoney() {
            return getBookedBalance().getExactValue().compareTo(BigDecimal.ZERO) < 0;
        }

        private ExactCurrencyAmount getAvailableCredit() {
            if (isCreditAccount()) {
                if (hasUsedAllOwnMoney()) {
                    return getAvailableBalance();
                } else {
                    return getCreditLimit();
                }
            }
            return ExactCurrencyAmount.zero(currency);
        }

        private ExactCurrencyAmount getAvailableBalance() {
            return ExactCurrencyAmount.of(availableBalance, currency);
        }

        private ExactCurrencyAmount getBookedBalance() {
            return ExactCurrencyAmount.of(bookedBalance, currency);
        }

        private ExactCurrencyAmount getCreditLimit() {
            return ExactCurrencyAmount.of(creditLimit, currency);
        }
    }
}
