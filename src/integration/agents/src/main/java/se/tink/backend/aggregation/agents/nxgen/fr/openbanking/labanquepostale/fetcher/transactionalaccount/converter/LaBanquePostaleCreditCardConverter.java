package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.CashAccountType;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LaBanquePostaleCreditCardConverter {

    public static CreditCardAccount toTinkCreditCard(AccountEntity accountEntity) {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(accountEntity.getAccountId().getIban())
                                .withBalance(getBalanceCreditCard(accountEntity))
                                .withAvailableCredit(ExactCurrencyAmount.of(0, "EUR"))
                                .withCardAlias(accountEntity.getName())
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountEntity.getResourceId())
                                .withAccountNumber(accountEntity.getLinkedAccount())
                                .withAccountName(accountEntity.getName())
                                .addIdentifier(
                                        new IbanIdentifier(accountEntity.getAccountId().getIban()))
                                .setProductName(accountEntity.getName())
                                .build())
                .setApiIdentifier(accountEntity.getResourceId())
                .build();
    }

    public static boolean isCreditCard(AccountEntity accountEntity) {
        return CashAccountType.CARD == accountEntity.getCashAccountType();
    }

    public static ExactCurrencyAmount getBalanceCreditCard(AccountEntity accountEntity) {
        return accountEntity.getBalances() != null && !accountEntity.getBalances().isEmpty()
                ? accountEntity.getBalances().get(0).getBalanceAmount().toAmount()
                : null;
    }
}
