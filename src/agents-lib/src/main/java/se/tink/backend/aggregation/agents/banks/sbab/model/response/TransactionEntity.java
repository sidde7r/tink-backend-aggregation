package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import java.util.Optional;
import com.google.common.base.Strings;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.TransactionTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {

    private static final AggregationLogger log = new AggregationLogger(TransactionEntity.class);

    @JsonProperty("Belopp")
    private String amount;

    @JsonProperty("Datum")
    private String date;

    @JsonProperty("Beskrivning")
    private String note;

    @JsonProperty("Typ")
    private String type;

    @JsonProperty("Mottagare")
    private String destinationAccountNumber;

    @JsonProperty("Meddelande")
    private String destinationMessage;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note.trim();
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber;
    }

    public String getDestinationMessage() {
        return destinationMessage;
    }

    public void setDestinationMessage(String destinationMessage) {
        this.destinationMessage = destinationMessage;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Optional<Transaction> toTinkTransaction() {
        try {
            Transaction transaction = new Transaction();

            transaction.setDescription(getNote() != null ? getNote().trim() : "");

            if (!Strings.isNullOrEmpty(getDate()) && !getDate().trim().isEmpty()) {
                transaction.setDate(DateUtils.parseDate(getDate()));
            } else {
                log.error("A transaction cannot have a null date");
                return Optional.empty();
            }

            if (!Strings.isNullOrEmpty(getAmount()) && !getAmount().trim().isEmpty()) {
                String cleanAmount = getAmount().replaceAll("[^\\d.,-]", "");
                double amount = StringUtils.parseAmount(cleanAmount);
                transaction.setAmount(amount);

                if (Objects.equal(amount, 0)) {
                    log.warn("Transaction amount (" + getAmount() + ") was parsed to 0.");
                }
            } else {
                log.error("A transaction cannot have a null amount");
                return Optional.empty();
            }

            transaction.setType(getTinkTransactionType());

            return Optional.of(transaction);

        } catch (Exception e) {
            log.error("Could not create transaction", e);
            return Optional.empty();
        }
    }

    private TransactionTypes getTinkTransactionType() {
        if (type == null) {
            return TransactionTypes.DEFAULT;
        }

        switch (type.toLowerCase()) {
        case "överföring":
            return TransactionTypes.TRANSFER;
        case "uttag":
        case "insättning":
            return TransactionTypes.DEFAULT;
        default:
            return TransactionTypes.DEFAULT;
        }

    }
}
