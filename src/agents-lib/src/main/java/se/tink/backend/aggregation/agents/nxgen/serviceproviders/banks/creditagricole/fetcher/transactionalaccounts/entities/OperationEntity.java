package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.entities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class OperationEntity {
    private String id;
    private String crId;
    private String bankOperationId;
    private String accountId;
    private String date;
    private String updateTimeStamp;
    private double amount;
    private String label;
    private String longLabel;
    private int categoryId;
    private boolean categoryCertified;
    private boolean checked;
    private String paymentMode;

    public Transaction toTinkTransaction() {

        Date parsedDate;
        try {
            parsedDate = new SimpleDateFormat(CreditAgricoleConstants.DATE_FORMAT).parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }

        return Transaction.builder()
                .setAmount(Amount.inEUR(amount))
                .setDate(parsedDate)
                .setDescription(label)
                .setRawDetails(this)
                .setExternalId(id)
                .build();
    }
}
