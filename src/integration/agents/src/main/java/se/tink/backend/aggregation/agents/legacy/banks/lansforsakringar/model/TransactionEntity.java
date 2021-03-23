package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.Locale;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.date.CountryDateHelper;

@JsonObject
public class TransactionEntity {
    @JsonIgnore
    private static CountryDateHelper dateHelper = new CountryDateHelper(new Locale("se", "SE"));

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    private AmountEntity amount;
    private String unconstrainedText;

    @JsonIgnore
    public Transaction toTransaction() {
        Transaction transaction = new Transaction();

        transaction.setDate(dateHelper.flattenTime(transactionDate));
        transaction.setDescription(unconstrainedText);
        transaction.setAmount(amount.getValue().doubleValue());

        return transaction;
    }
}
