package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.Transactions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class Transaction {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private String creditDebitIndicator;
    private String entryReference;
    private List<String> remittanceInformation;
    private String status;
    private TransactionAmount transactionAmount;

    public se.tink.backend.aggregation.nxgen.core.transaction.Transaction toTinkModel(
            TransactionalAccount account) {
        return se.tink.backend.aggregation.nxgen.core.transaction.Transaction.builder()
                .setAmount(getAmount())
                .setDate(getDate())
                .setPending(status.equalsIgnoreCase(Transactions.PENDING_STATUS))
                .build();
    }

    private Date getDate() {
        return bookingDate;
    }

    private Amount getAmount() {
        return new Amount(
                transactionAmount.getCurrency(), Double.parseDouble(transactionAmount.getAmount()));
    }
}
