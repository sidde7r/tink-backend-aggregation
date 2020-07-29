package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import com.google.common.base.Strings;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransferItem {
    private Object transferLastUpdate;
    private String amountType;
    private se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities.Amount
            amount;
    private String period;
    private se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities.Beneficiary
            beneficiary;
    private String originatingAccount;
    private String executionDate;
    private String transferType;
    private String transferId;
    private se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities.Communication
            communication;
    private static final AggregationLogger logger = new AggregationLogger(TransferItem.class);

    private final DateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

    private boolean isTransactionNegative() {
        return FortisConstants.NEGATIVE_TRANSACTION_TYPE.equalsIgnoreCase(amountType);
    }

    private Amount getTinkAmount() {
        try {
            Amount result =
                    new Amount(
                            amount.getCurrency(),
                            NumberFormat.getInstance(Locale.FRANCE).parse(amount.getAmount()));
            if (isTransactionNegative()) {
                return result.negate();
            }
            return result;
        } catch (ParseException e) {
            throw new IllegalStateException(
                    "Cannot parse amount in transaction: " + e.toString(), e);
        }
    }

    private java.util.Date getDate() {
        try {
            return simpleDateFormat.parse(executionDate);
        } catch (ParseException e) {
            throw new IllegalStateException(
                    "Cannot parse amount in transaction: " + e.toString(), e);
        }
    }

    private String getTransactionDescription() {
        StringBuilder builder = new StringBuilder();

        if (!Strings.isNullOrEmpty(transferType)) {
            builder.append(transferType.trim()).append(" ");
        }

        if (communication != null && !Strings.isNullOrEmpty(communication.getMessage())) {
            builder.append(communication.getMessage().trim()).append(" ");
        }

        if (beneficiary != null && !Strings.isNullOrEmpty(beneficiary.getName())) {
            builder.append(beneficiary.getName().trim()).append(" ");
        }

        return builder.toString().trim();
    }

    public boolean isValid() {
        try {
            toTinkTransaction();
            return true;
        } catch (RuntimeException e) {
            logger.errorExtraLong(
                    "Cannot parse transactions: ",
                    FortisConstants.LoggingTag.TRANSACTION_VALIDATION_ERROR,
                    e);
            return false;
        }
    }

    public UpcomingTransaction toTinkTransaction() {
        return UpcomingTransaction.builder()
                .setAmount(getTinkAmount())
                .setDate(getDate())
                .setDescription(getTransactionDescription())
                .build();
    }
}
