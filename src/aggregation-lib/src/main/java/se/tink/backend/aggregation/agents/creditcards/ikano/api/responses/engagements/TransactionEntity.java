package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.engagements;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.utils.IkanoParser;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.TransactionTypes;
import se.tink.libraries.date.DateUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    private double amount;
    private double originalAmount;
    private Date date;
    private String companyName;
    private String transactionName;

    public Transaction toTinkTransaction() {
        Transaction tinkTransaction = new Transaction();

        tinkTransaction.setType(TransactionTypes.CREDIT_CARD);
        tinkTransaction.setAmount(amount);
        tinkTransaction.setDate(date);
        tinkTransaction.setDescription(getDescription());
        tinkTransaction.setOriginalAmount(originalAmount);
        tinkTransaction.setPending(isPendingTransaction());

        return tinkTransaction;
    }

    private boolean isPendingTransaction() {
        return !Strings.isNullOrEmpty(transactionName) &&
                Objects.equals(transactionName.toLowerCase(), "skyddat belopp");
    }

    public double getAmount() {
        return amount;
    }

    public double getOriginalAmount() {
        return originalAmount;
    }

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        if (isPendingTransaction() || Strings.isNullOrEmpty(companyName)) {
            return transactionName;
        }

        return companyName;
    }

    @JsonProperty("_Amount")
    public void setAmount(String amount) {
        this.amount = -IkanoParser.stringToDouble(amount);
    }

    @JsonProperty("_OriginalAmount")
    public void setOriginalAmount(String originalAmount) {
        this.originalAmount = -IkanoParser.stringToDouble(originalAmount);
    }

    @JsonProperty("_TransDate")
    public void setDate(String date) throws ParseException {
        this.date = DateUtils.flattenTime(IkanoParser.stringToDate(date));
    }

    @JsonProperty("_CompanyName")
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @JsonProperty("_TransName")
    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }
}
