package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.date.DateUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {

    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected double amount;
    protected String text;
    protected Date transactiondate;
    private boolean preliminary;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTransactiondate() {
        return transactiondate;
    }

    public void setTransactiondate(Date transactiondate) {
        this.transactiondate = transactiondate;
    }

    public boolean isPreliminary() {
        return preliminary;
    }

    public void setPreliminary(boolean preliminary) {
        this.preliminary = preliminary;
    }

    public Transaction toTransaction() {
        Transaction transaction = new Transaction();

        transaction.setDate(DateUtils.flattenTime(transactiondate));
        transaction.setDescription(text);
        transaction.setAmount(amount);

        if (isPreliminary() || transaction.getDescription().equalsIgnoreCase("PREL KORTKÖP")) {
            transaction.setPending(true);
            // This check is used to see if we have any occurence of legacy way of finding pending
            // transactions for LF
            if (!isPreliminary()) {
                logger.info("[Found occurence of PendingStringTypes.LANSFORSAKRINGAR]");
            }
        }

        return transaction;
    }
}
