package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.Balance;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.CardAccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
public class DnbCardMapper {

    private static final String INTERIM_AVAILABLE = "interimAvailable";
    private static final String FORWARD_AVAILABLE = "forwardAvailable";

    public Optional<CreditCardAccount> toTinkCardAccount(CardAccountEntity cardAccountEntity) {
        CreditCardAccount cardAccount = null;
        try {
            cardAccount =
                    CreditCardAccount.nxBuilder()
                            .withCardDetails(
                                    CreditCardModule.builder()
                                            .withCardNumber(cardAccountEntity.getMaskedPan())
                                            .withBalance(getOutstandingBalance(cardAccountEntity))
                                            .withAvailableCredit(
                                                    getAvailableCredit(cardAccountEntity))
                                            .withCardAlias(cardAccountEntity.getName())
                                            .build())
                            .withPaymentAccountFlag()
                            .withId(
                                    IdModule.builder()
                                            .withUniqueIdentifier(cardAccountEntity.getMaskedPan())
                                            .withAccountNumber(cardAccountEntity.getResourceId())
                                            .withAccountName(cardAccountEntity.getName())
                                            .addIdentifier(
                                                    AccountIdentifier.create(
                                                            AccountIdentifier.Type
                                                                    .PAYMENT_CARD_NUMBER,
                                                            cardAccountEntity.getMaskedPan()))
                                            .build())
                            .setApiIdentifier(cardAccountEntity.getResourceId())
                            .build();
        } catch (RuntimeException e) {
            log.error("Failed to parse card account, it will be skipped.", e);
        }
        return Optional.ofNullable(cardAccount);
    }

    private ExactCurrencyAmount getOutstandingBalance(CardAccountEntity cardAccountEntity) {
        return getBalance(cardAccountEntity, INTERIM_AVAILABLE).negate();
    }

    private ExactCurrencyAmount getAvailableCredit(CardAccountEntity cardAccountEntity) {
        return getBalance(cardAccountEntity, FORWARD_AVAILABLE);
    }

    private ExactCurrencyAmount getBalance(CardAccountEntity cardAccountEntity, String type) {
        return cardAccountEntity.getBalances().stream()
                .filter(x -> type.equalsIgnoreCase(x.getBalanceType()))
                .findFirst()
                .map(Balance::toTinkAmount)
                .orElse(ExactCurrencyAmount.of(0.0, cardAccountEntity.getCurrency()));
    }
}
