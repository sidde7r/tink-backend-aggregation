
package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

@JsonObject
public class Transaction {

    private String bookingDate;
    private String creditDebitIndicator;
    private String entryReference;
    private List<String> remittanceInformation;
    private String status;
    private TransactionAmount transactionAmount;

    public se.tink.backend.aggregation.nxgen.core.transaction.Transaction toTinkModel(
        TransactionalAccount account) {
        return se.tink.backend.aggregation.nxgen.core.transaction.Transaction
            .builder()
            .setAmount(getAmount())
            .setDate(getDate())
            .setPending(
                status.equalsIgnoreCase(BnpParibasFortisConstants.Transactions.PENDING_STATUS))
            .build();
    }

    private Date getDate() {
        try {
            return ThreadSafeDateFormat.FORMATTER_DAILY.parse(bookingDate);
        } catch (ParseException e) {
            throw new IllegalStateException("Cannot parse date", e);
        }
    }

    private Amount getAmount() {
        return new Amount(transactionAmount.getCurrency(),
            Double.parseDouble(transactionAmount.getAmount()));
    }
}
