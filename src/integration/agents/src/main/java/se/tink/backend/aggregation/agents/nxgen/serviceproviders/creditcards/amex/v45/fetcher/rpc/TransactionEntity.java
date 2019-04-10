package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class TransactionEntity {
    private String suppIndex;
    private String type;
    private ChargeDateEntity chargeDate;
    private AmountEntity amount;
    private List<String> description;
    private ExtendedTransactionDetailsEntity extendedTransactionDetails;
    private ForeignTransactionDetailsEntity foreignTransactionDetailsEntity;

    public String getSuppIndex() {
        return suppIndex;
    }

    public Transaction toTransaction(AmericanExpressConfiguration config, boolean isPending) {
        Date date = new Date();
        try {
            date =
                    DateUtils.flattenTime(
                            DateUtils.flattenTime(
                                    ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.parse(
                                            Long.toString(chargeDate.getRawValue()))));
        } catch (ParseException ignored) {
        }

        return Transaction.builder()
                .setAmount(config.toAmount(-amount.getRawValue()))
                .setDate(date)
                .setPending(isPending)
                .setDescription(description.get(0))
                .build();
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public boolean belongsTo(CreditCardAccount account) {
        Integer transactionSuppIndex = -1;
        try {
            transactionSuppIndex = Integer.valueOf(this.suppIndex);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "transaction contains illegal suppIndex: "
                            + this.suppIndex
                            + "from transaction: "
                            + description
                            + "in account: "
                            + account.getAccountNumber());
        }

        return transactionSuppIndex.equals(Integer.valueOf(account.getBankIdentifier()));
    }
}
