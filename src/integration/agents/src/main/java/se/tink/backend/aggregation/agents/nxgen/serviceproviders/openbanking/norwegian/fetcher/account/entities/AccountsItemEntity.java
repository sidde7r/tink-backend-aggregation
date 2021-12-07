package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.fetcher.account.entities;

import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.exception.RequiredDataMissingException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.fetcher.account.rpc.BalanceResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.IdBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class AccountsItemEntity {

    private static final String SAVINGS_ACCOUNT = "SavingsAccount";
    private static final String CREDIT_CARD = "CreditCard";

    private String resourceId;
    private String iban;
    private String bban;
    private String name;
    private String ownerName;
    private String currency;
    private String product;

    public boolean isSavings() {
        return SAVINGS_ACCOUNT.equalsIgnoreCase(product);
    }

    public boolean isCreditCard() {
        return CREDIT_CARD.equalsIgnoreCase(product);
    }

    public Optional<TransactionalAccount> toTinkAccount(BalanceResponse balances) {
        if (!isSavings()) {
            return Optional.empty();
        }

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withInferredAccountFlags()
                .withBalance(getBalanceModule(balances))
                .withId(getIdModule())
                .addHolderName(ownerName)
                .setApiIdentifier(resourceId)
                .build();
    }

    public Optional<CreditCardAccount> toTinkCard(BalanceResponse balanceResponse) {
        if (!isCreditCard()) {
            return Optional.empty();
        }
        return Optional.of(
                CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber(bban)
                                        .withBalance(getAvailableCardBalance(balanceResponse))
                                        .withAvailableCredit(ExactCurrencyAmount.zero(currency))
                                        .withCardAlias(name)
                                        .build())
                        .withInferredAccountFlags()
                        .withId(getIdModule())
                        .addHolderName(ownerName)
                        .setApiIdentifier(resourceId)
                        .build());
    }

    private ExactCurrencyAmount getAvailableCardBalance(BalanceResponse balanceResponse) {
        List<BalancesItemEntity> balancesList =
                Optional.ofNullable(balanceResponse.getBalances()).orElse(Collections.emptyList());

        Optional<BalancesItemEntity> futureBalance =
                balancesList.stream().filter(BalancesItemEntity::isExpectedBalance).findFirst();

        Optional<BalancesItemEntity> closingBalance =
                balancesList.stream().filter(BalancesItemEntity::isClosingBalance).findFirst();

        BalancesItemEntity availableBalance;
        if (futureBalance.isPresent()) {
            availableBalance = futureBalance.get();
        } else if (closingBalance.isPresent()) {
            availableBalance = closingBalance.get();
        } else {
            throw new RequiredDataMissingException(
                    "Unable to map account, missing balance information");
        }

        return availableBalance.getAmountEntity().toAmount();
    }

    private IdModule getIdModule() {
        IdBuildStep idModule =
                IdModule.builder()
                        .withUniqueIdentifier(resourceId)
                        .withAccountNumber(getAccountNumber())
                        .withAccountName(name)
                        .addIdentifier(new BbanIdentifier(bban));

        if (!Strings.isNullOrEmpty(iban)) {
            idModule.addIdentifier(new IbanIdentifier(iban));
        }
        return idModule.build();
    }

    private BalanceModule getBalanceModule(BalanceResponse balances) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(getBookedBalance(balances.getBalances()));
        getAvailableBalance(balances.getBalances())
                .ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }

    public ExactCurrencyAmount getBookedBalance(List<BalancesItemEntity> balances) {
        return balances.stream()
                .filter(BalancesItemEntity::isClosingBalance)
                .findFirst()
                .map(BalancesItemEntity::toTinkAmount)
                .orElseThrow(
                        () -> new RequiredDataMissingException("No balance found in the response"));
    }

    public Optional<ExactCurrencyAmount> getAvailableBalance(List<BalancesItemEntity> balances) {
        return balances.stream()
                .filter(BalancesItemEntity::isAvailableBalance)
                .findFirst()
                .map(BalancesItemEntity::toTinkAmount);
    }

    private String getAccountNumber() {
        return Strings.isNullOrEmpty(iban) ? bban : iban;
    }
}
