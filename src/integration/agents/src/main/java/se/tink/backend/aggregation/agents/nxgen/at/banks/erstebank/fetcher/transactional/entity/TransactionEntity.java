package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.entity;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String id;
    private AmountEntity amount;
    private String title;
    private String subtitle;
    private String date;

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
        return !Strings.isNullOrEmpty(title)
                && !Strings.isNullOrEmpty(date)
                && amount != null
                && validDate();
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
        return ErsteBankConstants.Date.TODAY.equalsIgnoreCase(date);
    }

    private boolean isTomorrow(String date) {
        return ErsteBankConstants.Date.TOMORROW.equalsIgnoreCase(date);
    }

    private Date getTomorrowsDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        return c.getTime();
    }

    private boolean isYesterday(String date) {
        return ErsteBankConstants.Date.YESTERDAY.equalsIgnoreCase(date);
    }

    private Date toTinkDate() {
        try {
            return new SimpleDateFormat(ErsteBankConstants.Patterns.DATE_FORMAT).parse(getDate());
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

            logger.error(
                    "tag={} DateParsing error", ErsteBankConstants.LogTags.ERROR_DATE_PARSING, e);
            throw new IllegalArgumentException("Cannot parse date: " + e.toString(), e);
        }
    }

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setAmount(getAmount().getTinkBalance())
                .setDate(toTinkDate())
                .setDescription(getTitle())
                .build();
    }
}
