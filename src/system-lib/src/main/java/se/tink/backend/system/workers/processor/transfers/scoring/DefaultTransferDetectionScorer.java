package se.tink.backend.system.workers.processor.transfers.scoring;

import java.util.Date;

import org.joda.time.DateMidnight;
import org.joda.time.Days;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.StringUtils;

/**
 * Scores transactions based on description and original date.
 */
public class DefaultTransferDetectionScorer implements TransferDetectionScorer {

    public double getScore(Transaction left, Transaction right) {
        return getDescriptionScore(left, right) + getDateScore(left.getOriginalDate(), right.getOriginalDate());
    }

    /**
     * Score based on the days between the transactions. Score from "-INF" to 3
     * Score 3 on the same day
     * Score 2 on on one day difference
     * Score 1 on two days difference etc
     */
    private static int getDateScore(Date date1, Date date2) {
        DateMidnight d1 = new DateMidnight(date1);
        DateMidnight d2 = new DateMidnight(date2);
        int dateDistance = Days.daysBetween(d1, d2).getDays();

        return (3 - Math.abs(dateDistance));
    }

    /**
     * Scores the description similarity from 0 to 1.
     */
    private static double getDescriptionScore(Transaction left, Transaction right) {
        return StringUtils.getJaroWinklerDistance(left.getOriginalDescription(), right.getOriginalDescription());
    }
}
