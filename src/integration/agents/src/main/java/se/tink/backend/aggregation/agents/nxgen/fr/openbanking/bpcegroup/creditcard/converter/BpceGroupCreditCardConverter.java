package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.creditcard.converter;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts.BalanceType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts.OtherInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BpceGroupCreditCardConverter {

    public CreditCardAccount toCreditCardAccount(
            AccountEntity accountEntity, List<BalanceEntity> balances) {

        String cardNumber =
                Optional.ofNullable(accountEntity.getAccountId().getOther())
                        .map(OtherInformationEntity::getIdentification)
                        .orElseThrow(() -> new IllegalStateException("Cannot map card number"));

        return CreditCardAccount.nxBuilder()
                .withCardDetails(mapCardDetails(accountEntity, cardNumber, balances))
                .withInferredAccountFlags()
                .withId(mapIdModule(accountEntity, cardNumber))
                .setApiIdentifier(accountEntity.getResourceId())
                .build();
    }

    private CreditCardModule mapCardDetails(
            AccountEntity accountEntity, String cardNumber, List<BalanceEntity> balances) {

        return CreditCardModule.builder()
                .withCardNumber(cardNumber)
                .withBalance(getBalance(balances))
                .withAvailableCredit(ExactCurrencyAmount.zero("EUR"))
                .withCardAlias(accountEntity.getName())
                .build();
    }

    private IdModule mapIdModule(AccountEntity accountEntity, String cardNumber) {
        return IdModule.builder()
                .withUniqueIdentifier(accountEntity.getResourceId())
                .withAccountNumber(accountEntity.getLinkedAccount())
                .withAccountName(accountEntity.getName())
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifierType.PAYMENT_CARD_NUMBER, cardNumber))
                .build();
    }

    private ExactCurrencyAmount getBalance(List<BalanceEntity> balances) {
        return findBalanceByType(balances, BalanceType.CLBD)
                .map(Optional::of)
                .orElseGet(() -> findBalanceByType(balances, BalanceType.XPCD))
                .map(BalanceEntity::getBalanceAmount)
                .map(AmountEntity::toTinkAmount)
                .orElse(ExactCurrencyAmount.zero("EUR"));
    }

    private static Optional<BalanceEntity> findBalanceByType(
            List<BalanceEntity> balances, BalanceType type) {
        return balances.stream().filter(b -> type == b.getBalanceType()).findAny();
    }
}
