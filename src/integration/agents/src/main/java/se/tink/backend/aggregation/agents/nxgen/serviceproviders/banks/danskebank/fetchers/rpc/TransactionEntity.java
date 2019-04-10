package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.ParseException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class TransactionEntity {
    private String subPaymentType;
    private String typeOfPayment;
    private boolean hasMoreDetails;
    private String transactionKey;
    private String pcatIdSub;
    private String pcatId;
    private boolean reconciled;
    private boolean showReconciled;
    private int balanceDecimals;
    private double balance;
    private boolean showBalance;
    private String currency;
    private int amountDecimals;
    private double amount;
    private String advice;
    private String text;
    private String bookingDate;

    public String getSubPaymentType() {
        return subPaymentType;
    }

    public String getTypeOfPayment() {
        return typeOfPayment;
    }

    public boolean isHasMoreDetails() {
        return hasMoreDetails;
    }

    public String getTransactionKey() {
        return transactionKey;
    }

    public String getPcatIdSub() {
        return pcatIdSub;
    }

    public String getPcatId() {
        return pcatId;
    }

    public boolean isReconciled() {
        return reconciled;
    }

    public boolean isShowReconciled() {
        return showReconciled;
    }

    public int getBalanceDecimals() {
        return balanceDecimals;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isShowBalance() {
        return showBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public int getAmountDecimals() {
        return amountDecimals;
    }

    public double getAmount() {
        return amount;
    }

    public String getAdvice() {
        return advice;
    }

    public String getText() {
        return text;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    @JsonIgnore
    private boolean isPending() {
        return !showReconciled;
    }

    public Transaction toTinkTransaction() {
        Transaction.Builder transactionBuilder =
                Transaction.builder()
                        .setAmount(new Amount(currency, amount))
                        .setDescription(text)
                        .setPending(isPending());

        try {
            transactionBuilder.setDate(
                    ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.parse(bookingDate));
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }

        return transactionBuilder.build();
    }

    public UpcomingTransaction toTinkUpcomingTransaction() {
        UpcomingTransaction.Builder upcomingTransactionBuilder =
                UpcomingTransaction.builder()
                        .setAmount(new Amount(currency, amount))
                        .setDescription(text);

        try {
            upcomingTransactionBuilder.setDate(
                    ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.parse(bookingDate));
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }

        return upcomingTransactionBuilder.build();
    }
}
