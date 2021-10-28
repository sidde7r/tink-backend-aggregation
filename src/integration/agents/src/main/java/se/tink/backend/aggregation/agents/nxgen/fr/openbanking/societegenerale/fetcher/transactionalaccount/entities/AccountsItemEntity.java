package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.common.types.CashAccountType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.BalanceTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.CardDetails;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.OtherIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@JsonObject
public class AccountsItemEntity {

    private CashAccountType cashAccountType;

    @JsonProperty("accountId")
    private AccountIdEntity accountIdEntity;

    private String resourceId;

    private List<BalancesItemEntity> balances;

    @JsonProperty("_links")
    private LinksEntity links;

    private String usage;

    private String psuStatus;

    private String name;

    private String linkedAccount;

    private String bicFi;

    private String details;

    public String getResourceId() {
        return resourceId;
    }

    public CashAccountType getCashAccountType() {
        return cashAccountType;
    }

    public Optional<TransactionalAccount> toTinkTransactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(getAccountType())
                .withInferredAccountFlags()
                .withBalance(getBalanceModule())
                .withId(getIdModuleForTransactionalAccount(accountIdEntity))
                .setApiIdentifier(resourceId)
                .build();
    }

    public Optional<CreditCardAccount> toTinkCreditCard() {
        return Optional.of(
                CreditCardAccount.nxBuilder()
                        .withCardDetails(getCreditCardModule())
                        .withInferredAccountFlags()
                        .withId(getIdModuleForCreditCardAccount(accountIdEntity))
                        .setApiIdentifier(resourceId)
                        .build());
    }

    public boolean isCheckingAccount() {
        return CashAccountType.CACC == cashAccountType;
    }

    public boolean isCreditCard() {
        return CashAccountType.CARD == cashAccountType && CardDetails.CREDIT_CARD.equals(details);
    }

    public LinksEntity getLinks() {
        return links;
    }

    private TransactionalAccountType getAccountType() {
        return CashAccountType.CACC != cashAccountType
                ? TransactionalAccountType.SAVINGS
                : TransactionalAccountType.CHECKING;
    }

    private String getAccountName() {
        if (!Strings.isNullOrEmpty(name)) {
            return name;
        } else {
            return cashAccountType.toString();
        }
    }

    private IdModule getIdModuleForTransactionalAccount(AccountIdEntity accountIdEntity) {
        return IdModule.builder()
                .withUniqueIdentifier(accountIdEntity.getIban())
                .withAccountNumber(accountIdEntity.getIban())
                .withAccountName(getAccountName())
                .addIdentifier(new IbanIdentifier(accountIdEntity.getIban()))
                .build();
    }

    private IdModule getIdModuleForCreditCardAccount(AccountIdEntity accountIdEntity) {
        return IdModule.builder()
                .withUniqueIdentifier(resourceId)
                .withAccountNumber(linkedAccount)
                .withAccountName(name)
                .addIdentifier(new OtherIdentifier(accountIdEntity.getOther().getIdentification()))
                .setProductName(name)
                .build();
    }

    private CreditCardModule getCreditCardModule() {
        return CreditCardModule.builder()
                .withCardNumber(accountIdEntity.getOther().getIdentification())
                .withBalance(getBalance())
                .withAvailableCredit(getBalance())
                .withCardAlias(name)
                .build();
    }

    private BalanceModule getBalanceModule() {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(getBookedBalance());
        getAvailableBalance().ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }

    private ExactCurrencyAmount getBookedBalance() {
        if (balances.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot determine booked balance from empty list of balances.");
        }
        Optional<BalancesItemEntity> balanceEntity =
                balances.stream()
                        .filter(b -> BalanceTypes.CLBD.equalsIgnoreCase(b.getBalanceType()))
                        .findAny();

        if (!balanceEntity.isPresent()) {
            log.warn(
                    "Couldn't determine booked balance of known type, and no credit limit included. Defaulting to first provided balance.");
        }
        return balanceEntity
                .map(Optional::of)
                .orElseGet(() -> balances.stream().findFirst())
                .map(BalancesItemEntity::getBalanceAmount)
                .map(BalanceAmountEntity::getAmount)
                .get();
    }

    private Optional<ExactCurrencyAmount> getAvailableBalance() {
        return balances.stream()
                .filter(b -> BalanceTypes.XPCD.equalsIgnoreCase(b.getBalanceType()))
                .map(BalancesItemEntity::getBalanceAmount)
                .map(BalanceAmountEntity::getAmount)
                .findAny();
    }

    private ExactCurrencyAmount getBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .findFirst()
                .map(BalancesItemEntity::getBalanceAmount)
                .map(BalanceAmountEntity::getAmount)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SocieteGeneraleConstants.ErrorMessages.MISSING_BALANCE));
    }
}
