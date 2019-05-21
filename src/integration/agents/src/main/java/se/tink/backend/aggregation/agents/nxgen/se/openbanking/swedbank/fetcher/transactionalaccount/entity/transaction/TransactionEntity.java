package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {
    private String bookingDate;
    private String valueDate;
    private AmountEntity amount;
    private String remittanceInformationUnstructured;
    private TransactionBalanceEntity balance;

    public Amount getAmount() {
        return amount.toTinkAmount();
    }

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public TransactionBalanceEntity getBalance() {
        return balance;
    }

    public Date getBookingDate() {
        try {
            return new SimpleDateFormat(SwedbankConstants.Format.TRANSACTION_BOOKING_DATE_FORMAT)
                    .parse(bookingDate);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    public Date getValueDate() {
        try {
            return new SimpleDateFormat(SwedbankConstants.Format.TRANSACTION_BOOKING_DATE_FORMAT)
                    .parse(valueDate);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }
}
