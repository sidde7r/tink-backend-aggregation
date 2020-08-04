package se.tink.backend.aggregation.agents.banks.sbab.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.date.DateUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @JsonProperty("transferId")
    private String id;

    private String accountNumberFrom;
    private String accountNumberTo;
    private String amount;
    private String transactionDate;
    private String transferType;

    @JsonProperty("narrativeFrom")
    private String descriptionFrom;

    @JsonProperty("narrativeTo")
    private String descriptionTo;

    private String transferStatus;
    private String fromAccountName;

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setDescriptionFrom(String descriptionFrom) {
        this.descriptionFrom = descriptionFrom;
    }

    public Optional<Transaction> toTinkTransaction(boolean isUpcoming) {
        try {
            Transaction transaction = new Transaction();

            transaction.setId(id);

            transaction.setDescription(
                    Stream.of(descriptionFrom, descriptionTo)
                            .filter(java.util.Objects::nonNull)
                            .findFirst()
                            .orElse("N/A"));

            if (!Strings.isNullOrEmpty(transactionDate) && !transactionDate.trim().isEmpty()) {
                transaction.setDate(DateUtils.parseDate(transactionDate));
            } else {
                logger.error("A transaction cannot have a null date");
                return Optional.empty();
            }

            if (!Strings.isNullOrEmpty(amount) && !amount.trim().isEmpty()) {
                String cleanAmount = amount.replaceAll("[^\\d.,-]", "");
                double amount = Double.valueOf(cleanAmount);
                transaction.setAmount(amount);

                if (Objects.equal(amount, 0)) {
                    logger.warn("Transaction amount (" + amount + ") was parsed to 0.");
                }
            } else {
                logger.error("A transaction cannot have a null amount");
                return Optional.empty();
            }

            transaction.setType(getTinkTransactionType());

            transaction.setUpcoming(isUpcoming);

            return Optional.of(transaction);

        } catch (Exception e) {
            logger.error("Could not create transaction", e);
            return Optional.empty();
        }
    }

    private TransactionTypes getTinkTransactionType() {
        if (transferType == null) {
            return TransactionTypes.DEFAULT;
        }

        switch (transferType.toLowerCase()) {
            case "sbabgiro":
            case "deposit":
                return TransactionTypes.TRANSFER;
            case "withdrawal":
                return TransactionTypes.WITHDRAWAL;
            default:
                return TransactionTypes.DEFAULT;
        }
    }
}
