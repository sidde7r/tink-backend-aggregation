package se.tink.backend.aggregation.agents.banks.sbab.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.date.DateUtils;

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonObject
public class TransactionEntity {

    @JsonProperty("transferId")
    private String id;

    private String accountNumberFrom;
    private String accountNumberTo;
    private String amount;
    private String releaseDate;
    private String postingDate;
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

            if (!Strings.isNullOrEmpty(releaseDate) && !releaseDate.trim().isEmpty()) {
                transaction.setDate(DateUtils.parseDate(releaseDate));
            } else {
                log.error("A transaction cannot have a null date");
                return Optional.empty();
            }

            if (!Strings.isNullOrEmpty(amount) && !amount.trim().isEmpty()) {
                String cleanAmount = amount.replaceAll("[^\\d.,-]", "");
                double amount = Double.valueOf(cleanAmount);
                transaction.setAmount(amount);

                if (Objects.equal(amount, 0)) {
                    log.warn("Transaction amount (" + amount + ") was parsed to 0.");
                }
            } else {
                log.error("A transaction cannot have a null amount");
                return Optional.empty();
            }

            transaction.setType(getTinkTransactionType());

            transaction.setUpcoming(isUpcoming);

            return Optional.of(transaction);

        } catch (Exception e) {
            log.error("Could not create transaction", e);
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
