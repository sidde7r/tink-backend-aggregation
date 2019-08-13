package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class TransactionEntity {
    @JsonProperty private String transactionId;
    @JsonProperty private String entryReference;
    @JsonProperty private String endToEndId;
    @JsonProperty private String mandateId;
    @JsonProperty private String checkId;
    @JsonProperty private String creditorId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    @JsonProperty private AmountEntity transactionAmount;
    @JsonProperty private List<ExchangeRateEntity> currencyExchange;

    @JsonProperty private String creditorName;
    @JsonProperty private AccountReferenceEntity creditorAccount;
    @JsonProperty private String ultimateCreditor;

    @JsonProperty private String debtorName;
    @JsonProperty private AccountReferenceEntity debtorAccount;
    @JsonProperty private String ultimateDebtor;

    @JsonProperty private String remittanceInformationUnstructured;
    @JsonProperty private String remittanceInformationStructured;

    @JsonProperty private String purposeCo; // ExternalPurpose1Co

    @JsonProperty
    private String bankTransactionCode; // ISO 20022 ExternalBankTransactionDomain1Code

    @JsonProperty private String proprietaryBankTransactionCode;

    @JsonProperty("_links")
    private Map<String, LinkEntity> links;

    @JsonIgnore
    public Optional<LinkEntity> getLink(String linkName) {
        if (links == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(links.get(linkName));
    }

    @JsonIgnore
    public Transaction toBookedTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toTinkAmount())
                .setDate(getDate())
                .setDescription(getDescription())
                .build();
    }

    @JsonIgnore
    public UpcomingTransaction toPendingTransaction() {
        return UpcomingTransaction.builder()
                .setAmount(transactionAmount.toTinkAmount())
                .setDate(getDate())
                .setDescription(getDescription())
                .build();
    }

    @JsonIgnore
    public String getDescription() {
        return Optional.ofNullable(remittanceInformationUnstructured)
                .orElse(remittanceInformationStructured);
    }

    @JsonIgnore
    private Date getDate() {
        if (bookingDate != null) {
            return bookingDate;
        } else if (valueDate != null) {
            return valueDate;
        } else {
            throw new IllegalStateException("Transaction has no date.");
        }
    }

    @JsonIgnore
    public String getEntryReference() {
        return entryReference;
    }
}
