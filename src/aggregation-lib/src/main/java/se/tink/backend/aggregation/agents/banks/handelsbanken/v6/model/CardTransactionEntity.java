package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.utils.SHBUtils;
import se.tink.backend.core.PendingStringTypes;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.TransactionTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardTransactionEntity {
    private AmountEntity amount;
    private String date;
    private String description;

    public AmountEntity getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public void setAmount(AmountEntity amount) {
        this.amount = amount;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Transaction toTransaction(CardEntity cardEntity) {
        Transaction transaction = new Transaction();

        transaction.setDescription(SHBUtils.unescapeAndCleanTransactionDescription(description));
        transaction.setAmount(StringUtils.parseAmount(amount.getAmountFormatted()));
        transaction.setDate(DateUtils.flattenTime(DateUtils.parseDate(date)));

        if (transaction.getDescription().toLowerCase().startsWith("prel ")
                || transaction.getDescription().toLowerCase().startsWith("prel.")) {
            transaction.setDescription(SHBUtils.unescapeAndCleanTransactionDescription(transaction.getDescription()
                    .substring(5)));
            transaction.setPending(true);
        } else if (transaction.getDescription().equalsIgnoreCase(PendingStringTypes.HANDELSBANKEN.getValue())) {
            transaction.setPending(true);
        }

        transaction.setType(TransactionTypes.CREDIT_CARD);

        if (cardEntity != null && cardEntity.hasInvertedTransactions()) {
            transaction.setAmount(-transaction.getAmount());
        }

        return transaction;
    }
}
