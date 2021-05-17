package se.tink.backend.aggregation.agents.legacy.banks.seb.model;

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
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class UpcomingTransactionEntity {

    @JsonProperty("BETAL_DATUM")
    private String date;

    @JsonProperty("MOTTAGARE")
    private String displayDescription;

    @JsonProperty("UPPDRAG_BEL")
    private Double amount;

    @JsonProperty("KONTO_NR")
    private String accountNumber;

    // Used in toString method
    @JsonProperty("VERIF_TIME_STAMP")
    private String verifTimeStamp;

    @JsonProperty("VERIF_SUB_ID")
    private String verifSubId;

    @JsonProperty("KTOSLAG_TXT")
    private String ktoslagTxt;

    @JsonProperty("UPPDRAGS_TYP")
    private String uppdragsTyp;

    @JsonProperty("UPPDAT_FLG")
    private String uppdatFlg;

    @JsonProperty("REG_TIMESTAMP")
    private String regTimestamp;

    @JsonProperty("MOTTAGARE_PREFIX")
    private String mottagarePrefix;

    public static List<Transaction> toTransactionsForAccount(
            List<UpcomingTransactionEntity> upcomingTransactionEntities,
            final AccountEntity account) {

        Predicate<UpcomingTransactionEntity> isMatchingAccount =
                upcomingTransactionEntity ->
                        Objects.equal(account.KONTO_NR, upcomingTransactionEntity.accountNumber);

        return FluentIterable.from(upcomingTransactionEntities)
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
            Date dateToFlatten = ThreadSafeDateFormat.FORMATTER_DAILY.parse(this.date);
            return DateUtils.flattenTime(dateToFlatten);
        } catch (ParseException pe) {
            log.warn(
                    String.format(
                            "[Expected format for date]: YYYY-MM-DD [Found]: %s [Account number non-empty]: %s [Amount]: %s [Description non-empty]: %s",
                            this.date,
                            !Strings.isNullOrEmpty(this.accountNumber),
                            this.amount,
                            !Strings.isNullOrEmpty(this.displayDescription)),
                    pe);
            throw pe;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("date", date)
                .add("displayDescription", displayDescription)
                .add("amount", amount)
                .add("VERIF_TIME_STAMP", verifTimeStamp)
                .add("VERIF_SUB_ID", verifSubId)
                .add("KTOSLAG_TXT", ktoslagTxt)
                .add("UPPDRAGS_TYP", uppdragsTyp)
                .add("UPPDAT_FLG", uppdatFlg)
                .add("REG_TIMESTAMP", regTimestamp)
                .add("MOTTAGARE_PREFIX", mottagarePrefix)
                .toString();
    }
}
