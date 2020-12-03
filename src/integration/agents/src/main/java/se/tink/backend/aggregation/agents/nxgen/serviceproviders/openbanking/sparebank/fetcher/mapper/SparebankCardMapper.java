package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.mapper;

import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
public class SparebankCardMapper {

    private static final List<String> BALANCE_TYPES_PRIORITY =
            ImmutableList.of(
                    "forwardAvailable",
                    "interimAvailable",
                    "expected",
                    "openingBooked",
                    "closingBooked");

    public Optional<CreditCardAccount> toTinkCardAccount(
            CardEntity cardEntity, List<BalanceEntity> balanceEntities) {

        CreditCardAccount cardAccount = null;
        try {
            cardAccount =
                    CreditCardAccount.nxBuilder()
                            .withCardDetails(
                                    CreditCardModule.builder()
                                            .withCardNumber(cardEntity.getMaskedPan())
                                            .withBalance(
                                                    getOutstandingBalance(
                                                            balanceEntities, cardEntity))
                                            .withAvailableCredit(
                                                    getAvailableCredit(
                                                            balanceEntities,
                                                            cardEntity.getCurrency()))
                                            .withCardAlias(cardEntity.getProduct())
                                            .build())
                            .withPaymentAccountFlag()
                            .withId(
                                    IdModule.builder()
                                            .withUniqueIdentifier(cardEntity.getMaskedPan())
                                            .withAccountNumber(cardEntity.getMaskedPan())
                                            .withAccountName(cardEntity.getProduct())
                                            .addIdentifier(
                                                    AccountIdentifier.create(
                                                            AccountIdentifier.Type
                                                                    .PAYMENT_CARD_NUMBER,
                                                            cardEntity.getMaskedPan()))
                                            .build())
                            .setApiIdentifier(cardEntity.getResourceId())
                            .build();
        } catch (RuntimeException e) {
            log.error("Failed to parse card account, it will be skipped.", e);
        }
        return Optional.ofNullable(cardAccount);
    }

    private ExactCurrencyAmount getOutstandingBalance(
            List<BalanceEntity> balanceEntities, CardEntity cardEntity) {
        BigDecimal creditLimit = cardEntity.getCreditLimit().toAmount().getExactValue();
        BigDecimal availableCredit =
                getAvailableCredit(balanceEntities, cardEntity.getCurrency()).getExactValue();

        return ExactCurrencyAmount.of(
                creditLimit.subtract(availableCredit), cardEntity.getCurrency());
    }

    private ExactCurrencyAmount getAvailableCredit(
            List<BalanceEntity> balanceEntities, String accountCurrency) {

        for (String balanceType : BALANCE_TYPES_PRIORITY) {
            Optional<BalanceEntity> balanceEntity =
                    balanceEntities.stream()
                            .filter(x -> balanceType.equalsIgnoreCase(x.getBalanceType()))
                            .findAny();
            if (balanceEntity.isPresent()) {
                return balanceEntity.get().toAmount();
            }
        }
        return ExactCurrencyAmount.zero(accountCurrency);
    }
}
