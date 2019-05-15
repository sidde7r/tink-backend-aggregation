package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConstants.TransactionType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCard;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

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
        Amount amount = new Amount(billingCurrency, billingAmount);
        boolean isPending = TransactionType.AUTHORIZATION.equals(type);
        ZonedDateTime dateTime =
                ZonedDateTime.parse(transactionDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return CreditCardTransaction.builder()
                .setCreditCard(CreditCard.create(cardHolderName, cardNumber))
                .setDescription(description)
                .setPending(isPending)
                .setAmount(amount)
                .setDateTime(dateTime)
                .build();
    }
}
