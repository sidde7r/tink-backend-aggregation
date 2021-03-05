package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.Transactions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private String creditDebitIndicator;
    private String entryReference;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> remittanceInformation;

    private String status;
    private TransactionAmount transactionAmount;

    public Transaction toTinkModel() {
        return Transaction.builder()
                .setDescription(String.join(", ", remittanceInformation))
                .setAmount(getAmount())
                .setDate(bookingDate)
                .setPending(status.equalsIgnoreCase(Transactions.PENDING_STATUS))
                .build();
    }

    private ExactCurrencyAmount getAmount() {
        return new ExactCurrencyAmount(
                new BigDecimal(transactionAmount.getAmount()), transactionAmount.getCurrency());
    }
}
