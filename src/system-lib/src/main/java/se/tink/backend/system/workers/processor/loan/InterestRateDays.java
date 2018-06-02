package se.tink.backend.system.workers.processor.loan;

import com.google.common.collect.Maps;
import com.google.common.base.Preconditions;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class InterestRateDays {
    public static class DanskeBank {

        private final static DateFormat DF = new SimpleDateFormat("yyyy-MM");
        private static Map<String, Integer> mortgageDaysByMonth;
        private static Map<String, Integer> blancoDaysByMonth;

        static {
            mortgageDaysByMonth = Maps.newHashMap();
            blancoDaysByMonth = Maps.newHashMap();

            // Ask Danske Bank (through support) how räntedagar of 20xx will be distributed
            // Mortgage days are arbitrary, Blanco Johannes can calculate

            mortgageDaysByMonth.put("2018-01", 32);
            mortgageDaysByMonth.put("2018-02", 27);
            mortgageDaysByMonth.put("2018-03", 35);
            mortgageDaysByMonth.put("2018-04", 27);
            mortgageDaysByMonth.put("2018-05", 31);
            mortgageDaysByMonth.put("2018-06", 31);
            mortgageDaysByMonth.put("2018-07", 29);
            mortgageDaysByMonth.put("2018-08", 30);
            mortgageDaysByMonth.put("2018-09", 30);
            mortgageDaysByMonth.put("2018-10", 30);
            mortgageDaysByMonth.put("2018-11", 29);
            mortgageDaysByMonth.put("2018-12", 28);

            blancoDaysByMonth.put("2018-01", 33);
            blancoDaysByMonth.put("2018-02", 28);
            blancoDaysByMonth.put("2018-03", 34);
            blancoDaysByMonth.put("2018-04", 27);
            blancoDaysByMonth.put("2018-05", 31);
            blancoDaysByMonth.put("2018-06", 32);
            blancoDaysByMonth.put("2018-07", 29);
            blancoDaysByMonth.put("2018-08", 31);
            blancoDaysByMonth.put("2018-09", 31);
            blancoDaysByMonth.put("2018-10", 30);
            blancoDaysByMonth.put("2018-11", 30);
            blancoDaysByMonth.put("2018-12", 28);

            mortgageDaysByMonth.put("2017-01", 31);
            mortgageDaysByMonth.put("2017-02", 27);
            mortgageDaysByMonth.put("2017-03", 33);
            mortgageDaysByMonth.put("2017-04", 31);
            mortgageDaysByMonth.put("2017-05", 29);
            mortgageDaysByMonth.put("2017-06", 29);
            mortgageDaysByMonth.put("2017-07", 31);
            mortgageDaysByMonth.put("2017-08", 30);
            mortgageDaysByMonth.put("2017-09", 31);
            mortgageDaysByMonth.put("2017-10", 29);
            mortgageDaysByMonth.put("2017-11", 29);
            mortgageDaysByMonth.put("2017-12", 29);

            blancoDaysByMonth.put("2017-01", 32);
            blancoDaysByMonth.put("2017-02", 28);
            blancoDaysByMonth.put("2017-03", 32);
            blancoDaysByMonth.put("2017-04", 31);
            blancoDaysByMonth.put("2017-05", 30);
            blancoDaysByMonth.put("2017-06", 30);
            blancoDaysByMonth.put("2017-07", 31);
            blancoDaysByMonth.put("2017-08", 31);
            blancoDaysByMonth.put("2017-09", 32);
            blancoDaysByMonth.put("2017-10", 29);
            blancoDaysByMonth.put("2017-11", 30);
            blancoDaysByMonth.put("2017-12", 29);

            mortgageDaysByMonth.put("2016-01", 31);
            mortgageDaysByMonth.put("2016-02", 28);
            mortgageDaysByMonth.put("2016-03", 32);
            mortgageDaysByMonth.put("2016-04", 31);
            mortgageDaysByMonth.put("2016-05", 29);
            mortgageDaysByMonth.put("2016-06", 29);
            mortgageDaysByMonth.put("2016-07", 31);
            mortgageDaysByMonth.put("2016-08", 30);
            mortgageDaysByMonth.put("2016-09", 29);
            mortgageDaysByMonth.put("2016-10", 31);
            mortgageDaysByMonth.put("2016-11", 29);
            mortgageDaysByMonth.put("2016-12", 30);

            blancoDaysByMonth.put("2016-01", 33);
            blancoDaysByMonth.put("2016-02", 28);
            blancoDaysByMonth.put("2016-03", 31);
            blancoDaysByMonth.put("2016-04", 32);
            blancoDaysByMonth.put("2016-05", 29);
            blancoDaysByMonth.put("2016-06", 30);
            blancoDaysByMonth.put("2016-07", 32);
            blancoDaysByMonth.put("2016-08", 30);
            blancoDaysByMonth.put("2016-09", 30);
            blancoDaysByMonth.put("2016-10", 31);
            blancoDaysByMonth.put("2016-11", 30);
            blancoDaysByMonth.put("2016-12", 30);

            mortgageDaysByMonth.put("2015-01", 32);
            mortgageDaysByMonth.put("2015-02", 30);
            mortgageDaysByMonth.put("2015-03", 29);
            mortgageDaysByMonth.put("2015-04", 29);
            mortgageDaysByMonth.put("2015-05", 31);
            mortgageDaysByMonth.put("2015-06", 29);
            mortgageDaysByMonth.put("2015-07", 31);
            mortgageDaysByMonth.put("2015-08", 30);
            mortgageDaysByMonth.put("2015-09", 29);
            mortgageDaysByMonth.put("2015-10", 32);
            mortgageDaysByMonth.put("2015-11", 28);
            mortgageDaysByMonth.put("2015-12", 30);

            blancoDaysByMonth.put("2015-01", 34);
            blancoDaysByMonth.put("2015-02", 28);
            blancoDaysByMonth.put("2015-03", 29);
            blancoDaysByMonth.put("2015-04", 30);
            blancoDaysByMonth.put("2015-05", 32);
            blancoDaysByMonth.put("2015-06", 29);
            blancoDaysByMonth.put("2015-07", 31);
            blancoDaysByMonth.put("2015-08", 31);
            blancoDaysByMonth.put("2015-09", 30);
            blancoDaysByMonth.put("2015-10", 33);
            blancoDaysByMonth.put("2015-11", 28);
            blancoDaysByMonth.put("2015-12", 30);

            mortgageDaysByMonth.put("2014-01", 31);
            mortgageDaysByMonth.put("2014-02", 27);
            mortgageDaysByMonth.put("2014-03", 33);
            mortgageDaysByMonth.put("2014-04", 29);
            mortgageDaysByMonth.put("2014-05", 32);
            mortgageDaysByMonth.put("2014-06", 28);
            mortgageDaysByMonth.put("2014-07", 31);
            mortgageDaysByMonth.put("2014-08", 30);
            mortgageDaysByMonth.put("2014-09", 29);
            mortgageDaysByMonth.put("2014-10", 31);
            mortgageDaysByMonth.put("2014-11", 30);
            mortgageDaysByMonth.put("2014-12", 29);

            blancoDaysByMonth.put("2014-02", 28);
            blancoDaysByMonth.put("2014-03", 31);
            blancoDaysByMonth.put("2014-04", 30);
            blancoDaysByMonth.put("2014-05", 33);
            blancoDaysByMonth.put("2014-06", 28);
            blancoDaysByMonth.put("2014-07", 31);
            blancoDaysByMonth.put("2014-08", 32);
            blancoDaysByMonth.put("2014-09", 29);
            blancoDaysByMonth.put("2014-10", 31);
            blancoDaysByMonth.put("2014-11", 31);
            blancoDaysByMonth.put("2014-12", 29);

        }

        public static int getBlancoInterestRateDays(String transactionDate) {
            try {
                return getBlancoInterestRateDays(ThreadSafeDateFormat.FORMATTER_DAILY.parse(transactionDate));
            } catch (ParseException e) {
                throw new IllegalArgumentException("transactionDate should be on form: yyyy-MM-dd");
            }
        }

        public static int getBlancoInterestRateDays(Date transactionDate) {

            Date date = DateUtils.addDays(transactionDate, -10);
            String key = DF.format(date);
            Integer i = blancoDaysByMonth.get(key);

            // Will start throwing exceptions if we forget to fill mortgageDaysByMonth with consecutive years
            Preconditions.checkNotNull(i);

            return i;
        }

        public static int getMortgageInterestRateDays(String transactionDate) {
            try {
                return getMortgageInterestRateDays(ThreadSafeDateFormat.FORMATTER_DAILY.parse(transactionDate));
            } catch (ParseException e) {
                throw new IllegalArgumentException("transactionDate should be on form: yyyy-MM-dd");
            }
        }

        public static int getMortgageInterestRateDays(Date transactionDate) {

            Date date = DateUtils.addDays(transactionDate, -10);
            String key = DF.format(date);
            Integer i = mortgageDaysByMonth.get(key);

            // Will start throwing exceptions if we forget to fill mortgageDaysByMonth with consecutive years
            Preconditions.checkNotNull(i);

            return i;
        }
    }
}
