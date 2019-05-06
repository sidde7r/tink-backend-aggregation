package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    private TransactionsLinksEntity _links;
    private String additionalInformation;
    private String bankTransactionCode;
    private Date bookingDate;
    private String checkId;
    private TransactionsAccountEntity creditorAccount;
    private String creditorId;
    private String creditorName;
    private List<CurrencyExchangeEntity> currencyExchange;
    private TransactionsAccountEntity debtorAccount;
    private String debtorName;
    private String endToEndId;
    private String entryReference;
    private String mandateId;
    private String proprietaryBankTransactionCode;
    private String purposeCode;
    private String remittanceInformationStructured;
    private String remittanceInformationUnstructured;
    private AmountEntity transactionAmount;
    private String transactionId;
    private String ultimateCreditor;
    private String ultimateDebtor;
    private String valueDate;

    @JsonIgnore
    public Transaction toBookedTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(entryReference)
                .setPending(false)
                .build();
    }

    @JsonIgnore
    public Transaction toPendingTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(entryReference)
                .setPending(true)
                .build();
    }
}
