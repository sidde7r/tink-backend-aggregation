package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.entities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

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
            throw new RuntimeException(e.getMessage(), e);
        }

        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.inEUR(amount))
                .setDate(parsedDate)
                .setDescription(label)
                .setRawDetails(this)
                .setType(getTransactionType())
                .build();
    }

    private TransactionTypes getTransactionType() {
        if (StringUtils.isBlank(paymentMode)) {
            return TransactionTypes.DEFAULT;
        }

        switch (paymentMode) {
            case "Prélèvement":
                return TransactionTypes.PAYMENT;
            case "Carte":
                return TransactionTypes.CREDIT_CARD;
            case "Virement":
                return TransactionTypes.TRANSFER;
            case "Retrait":
                return TransactionTypes.WITHDRAWAL;
            default:
                return TransactionTypes.DEFAULT;
        }
    }
}
