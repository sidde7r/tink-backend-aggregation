package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

public class MovementsItem {
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
    private static final AggregationLogger LOGGER = new AggregationLogger(MovementsItem.class);


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

    private Amount getTinkAmount() {
        try {
            return new Amount(currency, NumberFormat.getInstance(Locale.FRANCE).parse(amount));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Date getDate() {
        try {
            return FortisConstants.DATE.TRANSACTION_FORMAT.parse(executionDate);
        } catch (ParseException e) {
            LOGGER.errorExtraLong("Cannot parse transactions: ", FortisConstants.LOGTAG.DATE_PARSING_ERROR, e);
        }
        return null;
    }

    private String getTransactionDescription() {
        return movementText;
    }

    private String getExternalId() {
        return operationId;
    }

    public boolean isValid() {
        try {
            toTinkTransaction();
            return true;
        } catch (Exception e) {
            LOGGER.errorExtraLong("Cannot parse transactions: ", FortisConstants.LOGTAG.TRANSACTION_VALIDATION_ERROR, e);
            return false;
        }
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(getTinkAmount())
                .setDate(getDate())
                .setDescription(getTransactionDescription())
                .setExternalId(getExternalId())
                .build();
    }
}
