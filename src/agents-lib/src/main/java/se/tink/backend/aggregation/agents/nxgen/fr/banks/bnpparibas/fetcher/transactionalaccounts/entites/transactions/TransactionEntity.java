package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.utils.BnpParibasFormatUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    @JsonProperty("categorie")
    private String category;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @JsonProperty("dateOperation")
    private Date transactionDate;
    @JsonProperty("idCategorie")
    private int categoryId;
    @JsonProperty("idOperation")
    private String transactionId;
    @JsonProperty("libelleOperation")
    private String transactionLabel;
    @JsonProperty("montant")
    private TransactionAmountEntity amount;
    @JsonProperty("operationType")
    private TransactionTypeEntity transactionType;
    @JsonProperty("pointage")
    private int tally;

    @JsonIgnore
    public Transaction toTinkTransaction(){
        return Transaction.builder()
                .setDescription(getFormattedDescription())
                .setDate(transactionDate)
                .setAmount(amount.getTinkAmount())
                .build();
    }

    @JsonIgnore
    private String getFormattedDescription() {
        Matcher matcher = BnpParibasFormatUtils.TRANSACTION_DESCRIPTION_PATTERN.matcher(getTransactionLabel());

        if (matcher.find()) {
            return matcher.group(BnpParibasConstants.TransactionDescriptionFormatting.MERCHANT_NAME);
        }

        return getTransactionLabel();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionLabel() {
        return Optional.ofNullable(transactionLabel).orElse("");
    }

    public void setTransactionLabel(String transactionLabel) {
        this.transactionLabel = transactionLabel;
    }

    public TransactionAmountEntity getAmount() {
        return amount;
    }

    public void setAmount(TransactionAmountEntity amount) {
        this.amount = amount;
    }

    public TransactionTypeEntity getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionTypeEntity transactionType) {
        this.transactionType = transactionType;
    }

    public int getTally() {
        return tally;
    }

    public void setTally(int tally) {
        this.tally = tally;
    }
}
