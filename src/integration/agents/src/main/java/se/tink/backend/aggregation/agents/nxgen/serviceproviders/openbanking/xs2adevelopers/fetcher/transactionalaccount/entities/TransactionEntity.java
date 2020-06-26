package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.utils.berlingroup.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private Date bookingDate;
    private AccountEntity creditorAccount;
    private String creditorName;
    private String remittanceInformationUnstructured;
    private AmountEntity transactionAmount;
    private String transactionId;
    private String valueDate;
    private String debtorName;
    private AccountEntity debtorAccount;
    private String endToEndId;
    private String purposeCode;

    @JsonIgnore
    public Transaction toBookedTinkTransaction() {
        return Transaction.builder()
                .setDate(bookingDate)
                .setPending(false)
                .setDescription(getDescription())
                .setAmount(transactionAmount.toTinkAmount())
                .build();
    }

    @JsonIgnore
    public Transaction toPendingTinkTransaction() {
        return Transaction.builder()
                .setDate(bookingDate)
                .setPending(true)
                .setDescription(getDescription())
                .setAmount(transactionAmount.toTinkAmount())
                .build();
    }

    private String getDescription() {
        return Stream.of(debtorName, creditorName, remittanceInformationUnstructured, purposeCode)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
    }
}
