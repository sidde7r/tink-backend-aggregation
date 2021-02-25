package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities;

import java.math.BigDecimal;
import java.util.Optional;
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

        return Optional.ofNullable(
                CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber(creditCardResponse.getCard())
                                        .withBalance(getBalance(creditCardResponse))
                                        .withAvailableCredit(
                                                getAvailableBalance(creditCardResponse))
                                        .withCardAlias(description)
                                        .build())
                        .withInferredAccountFlags()
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(id)
                                        .withAccountNumber(getAccountNumber(creditCardResponse))
                                        .withAccountName("")
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                                        account))
                                        .build())
                        .addParties(new Party(creditCardResponse.getAccountHolder(), Role.HOLDER))
                        .setApiIdentifier(id)
                        .build());
    }

    public String getId() {
        return id;
    }

    private ExactCurrencyAmount getBalance(CreditCardResponse creditCardResponse) {
        if (availableBalance != null) {
            return ExactCurrencyAmount.of(availableBalance, currency)
                    .subtract(ExactCurrencyAmount.of(limit, currency));
        }
        return getCardBalance(creditCardResponse);
    }

    private ExactCurrencyAmount getAvailableBalance(CreditCardResponse creditCardResponse) {
        if (availableBalance != null) {
            return ExactCurrencyAmount.of(availableBalance, currency);
        }
        return getCardBalance(creditCardResponse);
    }

    private ExactCurrencyAmount getCardBalance(CreditCardResponse creditCardResponse) {
        return creditCardResponse.getAvailableAmount() == null
                ? ExactCurrencyAmount.of(
                        creditCardResponse.getAccumulatedAmount(), creditCardResponse.getCurrency())
                : ExactCurrencyAmount.of(
                        creditCardResponse.getAvailableAmount(), creditCardResponse.getCurrency());
    }

    private String getAccountNumber(CreditCardResponse creditCardResponse) {
        return CardTypes.CREDIT.equals(creditCardResponse.getCardType())
                ? creditCardResponse.getDomiciliationAccount()
                : creditCardResponse.getDebitAccount();
    }
}
