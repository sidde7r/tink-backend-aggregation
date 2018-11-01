package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.entity;

import com.google.common.base.Strings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    private String id;
    private AmountEntity amount;
    private String title;
    private String subtitle;
    private String date;
    private static final AggregationLogger LONGLOGGER = new AggregationLogger(TransactionEntity.class);

    public String getId() {
        return id;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDate() {
        return date;
    }

    public boolean isTransaction() {
        return amount != null;
    }

    public boolean isValidTransaction() {
        return !Strings.isNullOrEmpty(title) && !Strings.isNullOrEmpty(date) && amount != null && validDate();
    }

    private boolean validDate() {
        try {
            toTinkDate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Date getYesterdayDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1);
        return c.getTime();
    }

    private Date getTodaysDate() {
        return Calendar.getInstance().getTime();
    }

    private boolean isToday(String date) {
        return ErsteBankConstants.DATE.TODAY.equalsIgnoreCase(date);
    }

    private boolean isTomorrow(String date) {
        return ErsteBankConstants.DATE.TOMORROW.equalsIgnoreCase(date);
    }

    private Date getTomorrowsDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        return c.getTime();
    }

    private boolean isYesterday(String date) {
        return ErsteBankConstants.DATE.YESTERDAY.equalsIgnoreCase(date);
    }

    private Date toTinkDate() {
        try {
            return new SimpleDateFormat(ErsteBankConstants.PATTERN.DATE_FORMAT).parse(getDate());
        } catch (ParseException e) {

            if (isTomorrow(getDate())) {
                return getTomorrowsDate();
            }

            if (isToday(getDate())) {
                return getTodaysDate();
            }

            if (isYesterday(getDate())) {
                return getYesterdayDate();
            }

            LONGLOGGER.errorExtraLong("DateParsing error", ErsteBankConstants.LOGTAG.ERROR_DATE_PARSING, e);
            throw new IllegalArgumentException("Cannot parse date: " + e.toString());
        }
    }

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setAmount(getAmount().getTinkBalance())
                .setDate(toTinkDate())
                .setDescription(getTitle())
                .setExternalId(id)
                .build();

    }
}
