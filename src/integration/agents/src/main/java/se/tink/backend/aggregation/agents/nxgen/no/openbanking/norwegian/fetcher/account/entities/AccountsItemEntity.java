package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.entities;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.exception.RequiredDataMissingException;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.rpc.BalanceResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountsItemEntity {

    private static final String SAVINGS_ACCOUNT = "SavingsAccount";
    private static final String CREDIT_CARD = "CreditCard";

    @Getter private String resourceId;
    private String bban;
    private String name;
    private String currency;
    private String product;

    public boolean isSavings() {
        return SAVINGS_ACCOUNT.equalsIgnoreCase(product);
    }

    public boolean isCreditCard() {
        return CREDIT_CARD.equalsIgnoreCase(product);
    }

    public Optional<TransactionalAccount> toTinkAccount(BalanceResponse balanceResponse) {
        if (!isSavings()) {
            return Optional.empty();
        }

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withInferredAccountFlags()
                .withBalance(BalanceModule.of(getAvailableBalance(balanceResponse)))
                .withId(getIdModule())
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
                                        .withBalance(getAvailableBalance(balanceResponse))
                                        .withAvailableCredit(ExactCurrencyAmount.zero(currency))
                                        .withCardAlias(name)
                                        .build())
                        .withInferredAccountFlags()
                        .withId(getIdModule())
                        .setApiIdentifier(resourceId)
                        .build());
    }

    private ExactCurrencyAmount getAvailableBalance(BalanceResponse balanceResponse) {
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
        return IdModule.builder()
                .withUniqueIdentifier(resourceId)
                .withAccountNumber(bban)
                .withAccountName(name)
                .addIdentifier(new BbanIdentifier(bban))
                .build();
    }
}
