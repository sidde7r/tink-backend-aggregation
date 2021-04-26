package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities;

import java.math.BigDecimal;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.CardTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.rpc.CreditCardResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class CardEntity {
    private String id;
    private String productCode;
    private String description;
    private String currency;
    private BigDecimal availableBalance;
    private String status;
    private String expiry;
    private BigDecimal limit;
    private String account;
    private String pan;
    private String type;
    private boolean available;
    private String brand;

    public Optional<CreditCardAccount> toTinkCreditCard(CreditCardResponse creditCardResponse) {

        String accountNumber = creditCardResponse.getDomiciliationAccount();
        String holderAccount = creditCardResponse.getAccountHolder();

        return Optional.ofNullable(
                CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber(creditCardResponse.getCard())
                                        .withBalance(getBalance())
                                        .withAvailableCredit(getAvailableBalance())
                                        .withCardAlias(description)
                                        .build())
                        .withInferredAccountFlags()
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(id)
                                        .withAccountNumber(accountNumber)
                                        .withAccountName("Account " + holderAccount)
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        AccountIdentifierType.IBAN,
                                                        accountNumber,
                                                        holderAccount))
                                        .build())
                        .addParties(new Party(holderAccount, Role.HOLDER))
                        .setApiIdentifier(id)
                        .build());
    }

    private ExactCurrencyAmount getBalance() {
        return ExactCurrencyAmount.of(availableBalance, currency)
                .subtract(ExactCurrencyAmount.of(limit, currency));
    }

    private ExactCurrencyAmount getAvailableBalance() {
        return ExactCurrencyAmount.of(availableBalance, currency);
    }

    public static boolean isCreditCard(CardEntity cardEntity) {
        return CardTypes.CREDIT.equals(cardEntity.getType());
    }
}
