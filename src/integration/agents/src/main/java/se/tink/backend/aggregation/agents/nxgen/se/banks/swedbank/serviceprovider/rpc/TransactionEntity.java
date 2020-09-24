package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Getter
@JsonObject
public class TransactionEntity extends AbstractTransactionEntity {
    private String id;
    private String expenseControlIncluded;
    private LabelingsEntity labelings;
    private CategorizationsEntity categorizations;
    private TransactionDetailsEntity details;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date accountingDate;

    private AmountEntity accountingBalance;

    public Optional<Transaction> toTinkTransaction() {
        if (this.date == null || getTransactionDescription() == null) {
            return Optional.empty();
        }

        double parsedAmount = AgentParsingUtils.parseAmount(amount);

        if (Strings.isNullOrEmpty(currency) || !Double.isFinite(parsedAmount)) {
            return Optional.empty();
        }

        Transaction.Builder transactionBuilder =
                Transaction.builder()
                        .setAmount(ExactCurrencyAmount.of(parsedAmount, currency))
                        .setDate(this.date)
                        .setPayload(
                                TransactionPayloadTypes.DETAILS,
                                SerializationUtils.serializeToString(getTransactionDetails()))
                        .setDescription(
                                SwedbankBaseConstants.Description.clean(
                                        getTransactionDescription()));

        if (SwedbankBaseConstants.Description.PENDING_TRANSACTIONS.contains(
                getTransactionDescription())) {
            transactionBuilder.setPending(true);
        }

        return Optional.of(transactionBuilder.build());
    }

    @JsonIgnore
    public TransactionDetails getTransactionDetails() {
        if (details != null) {
            return new TransactionDetails(details.getReference(), details.getMessage());
        }
        return new TransactionDetails(StringUtils.EMPTY, StringUtils.EMPTY);
    }
    // FIX temporary for Swedbanks pagination problems
    // create a kind of key to use to identify duplicate transactions
    public String getPseudoKey() {
        String key = date != null ? DateUtils.toJavaTimeLocalDate(date).toString() : "";
        key +=
                accountingDate != null
                        ? DateUtils.toJavaTimeLocalDate(accountingDate).toString()
                        : "";
        key += getAmount();
        key += getCurrency();
        key += getTransactionDescription();

        return key;
    }
}
