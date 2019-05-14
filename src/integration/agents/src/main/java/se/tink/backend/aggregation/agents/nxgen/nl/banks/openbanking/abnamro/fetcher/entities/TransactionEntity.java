package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.AbnAmroConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {

    private String mutationCode;
    private List<String> descriptionLines;
    private int balanceAfterMutation;
    private String counterPartyAccountNumber;
    private String counterPartyName;
    private double amount;
    private String currency;
    private String transactionId;

    @JsonFormat(pattern = AbnAmroConstants.TRANSACTION_BOOKING_DATE_FORMAT)
    private Date bookDate;

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setAmount(new Amount(currency, amount))
                .setDate(bookDate)
                .setDescription(String.join(" ", descriptionLines))
                .build();
    }
}
