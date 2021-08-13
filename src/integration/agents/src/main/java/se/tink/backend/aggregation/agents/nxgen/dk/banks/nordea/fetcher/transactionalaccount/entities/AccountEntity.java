package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.entities;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants.HOLDER_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AccountEntity {

    @JsonIgnore private final BalanceHelper balanceHelper = new BalanceHelper();

    private String accountId;
    private String iban;
    private String bic;
    private String nickname;
    private String displayAccountNumber;
    private String productCode;
    private String productName;
    private String registrationNumber;
    private String category;
    private Double bookedBalance;
    private Double availableBalance;
    private Double creditLimit;
    private String currency;
    private PermissionsEntity permissions;
    private List<RoleEntity> roles;

    public Optional<TransactionalAccount> toTinkAccount() {
        BalanceBuilderStep step =
                BalanceModule.builder()
                        .withBalance(balanceHelper.getExactBalance())
                        .setAvailableBalance(balanceHelper.calculateAvailableBalance())
                        .setAvailableCredit(balanceHelper.getAvailableCredit());
        balanceHelper.getCreditLimit().ifPresent(step::setCreditLimit);
        BalanceModule balanceModule = step.build();

        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(displayAccountNumber)
                        .withAccountNumber(displayAccountNumber)
                        .withAccountName(nickname)
                        .addIdentifier(new IbanIdentifier(bic, iban))
                        .addIdentifier(
                                new BbanIdentifier(registrationNumber + displayAccountNumber))
                        .setProductName(productName)
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
                .addParties(getParties())
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
            return bookedBalance != null ? getBookedBalance() : getAvailableBalance();
        }

        private boolean isCreditAccount() {
            return getCreditLimit()
                    .map(cl -> cl.getExactValue().compareTo(BigDecimal.ZERO) > 0)
                    .orElse(false);
        }

        private ExactCurrencyAmount tryAvailableBalanceOrBooked() {
            return availableBalance != null ? getAvailableBalance() : getBookedBalance();
        }

        private ExactCurrencyAmount calculateAvailableBalance() {
            if (isCreditAccount()) {
                if (hasUsedAllOwnMoney()) {
                    return ExactCurrencyAmount.zero(currency);
                } else {
                    return getAvailableBalance().subtract(getCreditLimit().orElse(currencyZero()));
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
                    return getCreditLimit().orElse(currencyZero());
                }
            }
            return currencyZero();
        }

        private ExactCurrencyAmount getAvailableBalance() {
            return ExactCurrencyAmount.of(availableBalance, currency);
        }

        private ExactCurrencyAmount getBookedBalance() {
            return ExactCurrencyAmount.of(bookedBalance, currency);
        }

        private Optional<ExactCurrencyAmount> getCreditLimit() {
            return Optional.ofNullable(creditLimit).map(cl -> ExactCurrencyAmount.of(cl, currency));
        }

        private ExactCurrencyAmount currencyZero() {
            return ExactCurrencyAmount.zero(currency);
        }
    }

    public List<Party> getParties() {
        return roles.stream().map(this::roleEntityToParty).collect(Collectors.toList());
    }

    private Party roleEntityToParty(RoleEntity roleEntity) {
        return new Party(
                roleEntity.getName(),
                HOLDER_TYPE_MAPPER.translate(roleEntity.getRole()).orElse(Party.Role.UNKNOWN));
    }
}
