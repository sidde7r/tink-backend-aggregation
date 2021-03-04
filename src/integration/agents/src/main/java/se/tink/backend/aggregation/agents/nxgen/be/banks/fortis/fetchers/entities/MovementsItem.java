package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class MovementsItem {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private boolean detailPresent;
    private String period;
    private String amount;
    private String sequenceNb;
    private String movementText;
    private boolean pending;
    private String executionDate;
    private String valueDate;
    private String description2;
    private List<Object> annexes;
    private String description1;
    private String otherParty;
    private String operationId;
    private String currency;

    private final DateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

    public boolean isDetailPresent() {
        return detailPresent;
    }

    public String getPeriod() {
        return period;
    }

    public String getAmount() {
        return amount;
    }

    public String getSequenceNb() {
        return sequenceNb;
    }

    public String getMovementText() {
        return movementText;
    }

    public boolean isPending() {
        return pending;
    }

    public String getExecutionDate() {
        return executionDate;
    }

    public String getValueDate() {
        return valueDate;
    }

    public String getDescription2() {
        return description2;
    }

    public List<Object> getAnnexes() {
        return annexes;
    }

    public String getDescription1() {
        return description1;
    }

    public String getOtherParty() {
        return otherParty;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getCurrency() {
        return currency;
    }

    private ExactCurrencyAmount getTinkAmount() {
        try {
            return ExactCurrencyAmount.of(
                    NumberFormat.getInstance(Locale.FRANCE).parse(amount.replace(".", "")),
                    currency);
        } catch (ParseException e) {
            throw new IllegalStateException(
                    "Cannot parse amount in transaction: " + e.toString(), e);
        }
    }

    private Date getDate() {
        try {
            return simpleDateFormat.parse(executionDate);
        } catch (ParseException e) {
            throw new IllegalStateException(
                    "Cannot parse amount in transaction: " + e.toString(), e);
        }
    }

    private String getDescription() {
        StringBuilder builder = new StringBuilder();

        if (!Strings.isNullOrEmpty(description1)) {
            builder.append(description1).append(" ");
        }

        if (!Strings.isNullOrEmpty(description2)) {
            builder.append(description2);
        }
        return builder.toString();
    }

    private String getTransactionDescription() {

        if (!Strings.isNullOrEmpty(getDescription())) {
            return getDescription();
        } else {
            return movementText;
        }
    }

    public boolean isValid() {
        try {
            toTinkTransaction();
            return true;
        } catch (RuntimeException e) {
            logger.error(
                    "tag={} Cannot parse transactions: ",
                    FortisConstants.LoggingTag.TRANSACTION_VALIDATION_ERROR,
                    e);
            return false;
        }
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(getTinkAmount())
                .setDate(getDate())
                .setDescription(getTransactionDescription())
                .build();
    }
}
