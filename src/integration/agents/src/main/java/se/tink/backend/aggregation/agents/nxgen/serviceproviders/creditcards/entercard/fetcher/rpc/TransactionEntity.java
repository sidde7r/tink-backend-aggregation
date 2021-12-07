package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConstants.TransactionType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.card.Card;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@SuppressWarnings("unused")
@JsonObject
public class TransactionEntity {

    private double transactionAmount;
    private String transactionCurrency;
    private double billingAmount;
    private String billingCurrency;
    private String city;
    private String country;

    private String transactionDate;
    private String description;
    private String cardHolderName;
    private int id;
    private boolean canBeSplitted;
    private String type;
    private Object paymentOptions;
    private String cardNumber;
    private boolean disputable;

    public Transaction toTinkTransaction() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of(billingAmount, billingCurrency);
        boolean isPending = TransactionType.AUTHORIZATION.equalsIgnoreCase(type);
        ZonedDateTime dateTime =
                ZonedDateTime.parse(transactionDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Builder builder =
                CreditCardTransaction.builder()
                        .setCreditCard(Card.create(cardHolderName, cardNumber))
                        .setDescription(description)
                        .setPending(isPending)
                        .setAmount(amount)
                        .setDateTime(dateTime);

        if (Objects.nonNull(id)) {
            builder.addExternalSystemIds(
                    TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                    Integer.toString(id));
        }

        return builder.build();
    }
}
