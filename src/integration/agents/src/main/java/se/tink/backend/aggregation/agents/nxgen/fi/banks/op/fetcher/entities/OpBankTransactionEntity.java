package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.DateUtils;

@JsonObject
public class OpBankTransactionEntity {
    private int order;
    private int page;
    private String recipient;
    private String payer;
    private List<String> transactionMessages;
    private String reference;
    private Date entryDate;
    private Date valueDate;
    private Date eventDate;
    private String amount;
    private String listIndex;
    private String explanation;
    private String timestamp;
    private String type;

    public Transaction toTransaction() {
        return Transaction.builder()
                .setAmount(Amount.inEUR(AgentParsingUtils.parseAmount(getAmount())))
                .setDescription(getDescription())
                .setDate(getDate())
                .setPending(getValueDate() == null)
                .build();
    }

    private Date getDate() {
        return DateUtils.parseDate(timestamp);
    }

    private String getDescription() {
        if (!Strings.isNullOrEmpty(getRecipient())) {
            return getRecipient();
        } else if (!Strings.isNullOrEmpty(getPayer())) {
            return getPayer();
        } else {
            return getExplanation();
        }
    }

    public int getOrder() {
        return order;
    }

    public OpBankTransactionEntity setOrder(int order) {
        this.order = order;
        return this;
    }

    public int getPage() {
        return page;
    }

    public OpBankTransactionEntity setPage(int page) {
        this.page = page;
        return this;
    }

    public String getRecipient() {
        return recipient;
    }

    public OpBankTransactionEntity setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public String getPayer() {
        return payer;
    }

    public OpBankTransactionEntity setPayer(String payer) {
        this.payer = payer;
        return this;
    }

    public List<String> getTransactionMessages() {
        return transactionMessages;
    }

    public OpBankTransactionEntity setTransactionMessages(List<String> transactionMessages) {
        this.transactionMessages = transactionMessages;
        return this;
    }

    public String getReference() {
        return reference;
    }

    public OpBankTransactionEntity setReference(String reference) {
        this.reference = reference;
        return this;
    }

    public Date getEntryDate() {
        return entryDate;
    }

    @JsonFormat(pattern = OpBankConstants.DATE_FORMAT)
    public OpBankTransactionEntity setEntryDate(Date entryDate) {
        this.entryDate = entryDate;
        return this;
    }

    public Date getValueDate() {
        return valueDate;
    }

    @JsonFormat(pattern = OpBankConstants.DATE_FORMAT)
    public OpBankTransactionEntity setValueDate(Date valueDate) {
        this.valueDate = valueDate;
        return this;
    }

    public Date getEventDate() {
        return eventDate;
    }

    @JsonFormat(pattern = OpBankConstants.DATE_FORMAT)
    public OpBankTransactionEntity setEventDate(Date eventDate) {
        this.eventDate = eventDate;
        return this;
    }

    public String getAmount() {
        return amount;
    }

    public OpBankTransactionEntity setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public String getListIndex() {
        return listIndex;
    }

    public OpBankTransactionEntity setListIndex(String listIndex) {
        this.listIndex = listIndex;
        return this;
    }

    public String getExplanation() {
        return explanation;
    }

    public OpBankTransactionEntity setExplanation(String explanation) {
        this.explanation = explanation;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public OpBankTransactionEntity setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getType() {
        return type;
    }

    public OpBankTransactionEntity setType(String type) {
        this.type = type;
        return this;
    }
}
