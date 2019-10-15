package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BookedItemEntity {

    @JsonProperty("debtorAccount")
    private DebtorAccountEntity debtorAccountEntity;

    @JsonProperty("creditorName")
    private String creditorName;

    @JsonProperty("bankTransactionCode")
    private String bankTransactionCode;

    @JsonProperty("creditorAccount")
    private CreditorAccountEntity creditorAccountEntity;

    @JsonProperty("transactionAmount")
    private TransactionAmountEntity transactionAmountEntity;

    @JsonProperty("proprietaryBankTransactionCode")
    private String proprietaryBankTransactionCode;

    @JsonProperty("bookingDate")
    private Date bookingDate;

    @JsonProperty("debtorName")
    private String debtorName;

    @JsonProperty("remittanceInformationUnstructured")
    private String remittanceInformationUnstructured;

    @JsonProperty("valueDate")
    private String valueDate;

    @JsonProperty("transactionId")
    private String transactionId;

    public Transaction toTinkTransactions() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS");

        Date date;
        try {
            date = formatter.parse(transactionId);
        } catch (ParseException e) {
            date = bookingDate;
        }

        return Transaction.builder()
                .setDate(date)
                .setAmount(
                        new ExactCurrencyAmount(
                                transactionAmountEntity.getAmount(),
                                transactionAmountEntity.getCurrency()))
                .setDescription(remittanceInformationUnstructured)
                .setPending(false)
                .build();
    }
}
