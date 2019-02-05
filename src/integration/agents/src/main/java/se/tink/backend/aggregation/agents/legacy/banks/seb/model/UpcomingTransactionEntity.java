package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpcomingTransactionEntity {
    private static final AggregationLogger log = new AggregationLogger(UpcomingTransactionEntity.class);

    @JsonProperty("BETAL_DATUM")
    public String date;
    @JsonProperty("MOTTAGARE")
    public String displayDescription;
    @JsonProperty("UPPDRAG_BEL")
    public Double amount;
    @JsonProperty("KONTO_NR")
    public String accountNumber;

    // --- Unused properties that could be of use ---
    // Long ROW_ID;
    public String VERIF_TIME_STAMP;
    public String VERIF_SUB_ID;
    // String SEB_KUND_NR;
    public String KTOSLAG_TXT;
    //public String KHAV;
    //public String UPPDRAGS_ID;
    public String UPPDRAGS_TYP;
    //public Double BOKF_SALDO;
    //public Double DISP_BEL;
    //public Double KREDBEL;
    public String UPPDAT_FLG;
    public String REG_TIMESTAMP;
    // String TABORT_ID;
    public String MOTTAGARE_PREFIX;

    public static List<Transaction> toTransactionsForAccount(
            List<UpcomingTransactionEntity> upcomingTransactionEntities, final AccountEntity account) {

        Predicate<UpcomingTransactionEntity> isMatchingAccount = upcomingTransactionEntity -> Objects
                .equal(account.KONTO_NR, upcomingTransactionEntity.accountNumber);

        return FluentIterable
                .from(upcomingTransactionEntities)
                .filter(isMatchingAccount)
                .transform(UpcomingTransactionEntity.TO_PENDING_TRANSACTION)
                .filter(Predicates.<Transaction>notNull())
                .toList();
    }

    private static final Function<UpcomingTransactionEntity, Transaction> TO_PENDING_TRANSACTION =
            new Function<UpcomingTransactionEntity, Transaction>() {
                @Override
                @Nullable
                public Transaction apply(UpcomingTransactionEntity upcomingTransactionEntity) {
                    try {
                        return upcomingTransactionEntity.toPendingTransaction();
                    } catch (ParseException e) {
                        log.error("Could not parse SEB upcoming transaction date", e);
                        log.error(upcomingTransactionEntity.toString());
                        return null;
                    }
                }
            };

    private Transaction toPendingTransaction() throws ParseException {
        Transaction transaction = new Transaction();

        transaction.setDate(getFlattenedDate());
        transaction.setDescription(displayDescription.trim());
        transaction.setAmount(amount);

        // All upcoming transactions should be pending.
        transaction.setPending(true);

        return transaction;
    }

    private Date getFlattenedDate() throws ParseException {
        try {
            Date date = ThreadSafeDateFormat.FORMATTER_DAILY.parse(this.date);
            return DateUtils.flattenTime(date);
        } catch (ParseException pe) {
            log.warn(String.format(
                    "[Expected format for date]: YYYY-MM-DD [Found]: %s [Account number non-empty]: %s [Amount]: %s [Description non-empty]: %s",
                    this.date, !Strings.isNullOrEmpty(this.accountNumber), this.amount,
                    !Strings.isNullOrEmpty(this.displayDescription)), pe);
            throw pe;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("date", date)
                .add("displayDescription", displayDescription)
                .add("amount", amount)
                .add("VERIF_TIME_STAMP", VERIF_TIME_STAMP)
                .add("VERIF_SUB_ID", VERIF_SUB_ID)
                .add("KTOSLAG_TXT", KTOSLAG_TXT)
                .add("UPPDRAGS_TYP", UPPDRAGS_TYP)
                .add("UPPDAT_FLG", UPPDAT_FLG)
                .add("REG_TIMESTAMP", REG_TIMESTAMP)
                .add("MOTTAGARE_PREFIX", MOTTAGARE_PREFIX)
                .toString();
    }
}
