package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.creditcard.converter;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.exceptions.refresh.CreditCardAccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.accounts.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.accounts.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.accounts.BalanceType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BpceGroupCreditCardConverter {
    private static final Pattern CREDIT_CARD_REGEX = Pattern.compile("XX\\d{4}");

    public static CreditCardAccount toCreditCardAccount(
            AccountEntity accountEntity, List<BalanceEntity> balances) {

        String cardNumber = determineCardNumber(accountEntity);

        return CreditCardAccount.nxBuilder()
                .withCardDetails(mapCardDetails(accountEntity, cardNumber, balances))
                .withInferredAccountFlags()
                .withId(mapIdModule(accountEntity, cardNumber))
                .setApiIdentifier(accountEntity.getResourceId())
                .addHolderName(accountEntity.getHolderName())
                .build();
    }

    public static CreditCardAccount toCreditCardAccount(AccountEntity accountEntity) {
        return toCreditCardAccount(accountEntity, accountEntity.getBalances());
    }

    private static CreditCardModule mapCardDetails(
            AccountEntity accountEntity, String cardNumber, List<BalanceEntity> balances) {

        return CreditCardModule.builder()
                .withCardNumber(cardNumber)
                .withBalance(getBalance(balances))
                .withAvailableCredit(ExactCurrencyAmount.zero("EUR"))
                .withCardAlias(accountEntity.getProduct())
                .build();
    }

    private static IdModule mapIdModule(AccountEntity accountEntity, String cardNumber) {
        return IdModule.builder()
                .withUniqueIdentifier(accountEntity.getResourceId())
                .withAccountNumber(accountEntity.getLinkedAccount())
                .withAccountName(accountEntity.getProduct())
                .addIdentifier(new MaskedPanIdentifier(cardNumber))
                .build();
    }

    private static ExactCurrencyAmount getBalance(List<BalanceEntity> balances) {
        return findBalanceByType(balances, BalanceType.CLBD)
                .map(Optional::of)
                .orElseGet(() -> findBalanceByType(balances, BalanceType.XPCD))
                .map(BalanceEntity::getBalanceAmount)
                .map(AmountEntity::toTinkAmount)
                .orElseGet(() -> determineOtherBalance(balances));
    }

    /**
     * For OTHR type of balance in {@link BalanceEntity#name} two values are present: 'Encours' or
     * 'Dernier encours prélevé'. The first one of them means 'In progress' and therefore will be
     * used to determine balance for such card.
     *
     * @param balances
     * @return Balance taken from list which name is `Encours`. If such value won`t be found then
     *     zero amount will be returned.
     */
    private static ExactCurrencyAmount determineOtherBalance(List<BalanceEntity> balances) {
        return findBalanceByName(balances, "Encours")
                .map(BalanceEntity::getBalanceAmount)
                .map(AmountEntity::toTinkAmount)
                .orElse(ExactCurrencyAmount.zero("EUR"));
    }

    private static Optional<BalanceEntity> findBalanceByType(
            List<BalanceEntity> balances, BalanceType type) {
        return balances.stream().filter(b -> type == b.getBalanceType()).findAny();
    }

    private static Optional<BalanceEntity> findBalanceByName(
            List<BalanceEntity> balances, String name) {
        return balances.stream().filter(b -> name.equalsIgnoreCase(b.getName())).findAny();
    }

    /**
     * Returned value which can be used is available under {@link AccountEntity#name} and it looks
     * like "MLLE NAME SURNAME XX1234".
     *
     * @param accountEntity
     * @return extracted value
     */
    private static String determineCardNumber(AccountEntity accountEntity) {
        String name = accountEntity.getName();
        Matcher matcher = CREDIT_CARD_REGEX.matcher(name);
        if (matcher.find()) {
            return matcher.group(0);
        }
        throw new CreditCardAccountRefreshException("Cannot determine card number");
    }
}
