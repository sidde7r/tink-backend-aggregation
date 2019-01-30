package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.date.DateUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardTransactionEntity {
    private double amountHolderCurrency;
    private String text;
    private Date date;
    private Date debitDate;
    private boolean definitive;
    
    public double getAmountHolderCurrency() {
        return amountHolderCurrency;
    }

    public void setAmountHolderCurrency(double amountHolderCurrency) {
        this.amountHolderCurrency = amountHolderCurrency;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return DateUtils.flattenTime(date != null ? date : debitDate);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDebitDate() {
        return debitDate;
    }

    public void setDebitDate(Date debitDate) {
        this.debitDate = debitDate;
    }

    public boolean isDefinitive() {
        return definitive;
    }

    public void setDefinitive(boolean definitive) {
        this.definitive = definitive;
    }

    public Transaction toTransaction() {
        Transaction transaction = new Transaction();
        
        transaction.setDescription(text);
        transaction.setAmount(-amountHolderCurrency);
        transaction.setDate(getDate());
        transaction.setPending(!definitive);
        
        if (transaction.getAmount() < 0) {
            transaction.setType(TransactionTypes.CREDIT_CARD);
        }

        return transaction;
    }

}
