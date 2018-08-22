package se.tink.backend.aggregation.agents.banks.sbab.util;

import java.util.Calendar;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class SBABDateUtilTest {

    public static class MinuteBeforeMidday {

        static Calendar monday;

        static {
            monday = Calendar.getInstance();
            monday.set(2016, Calendar.JANUARY, 4);
        }

        @Test
        public void mondayInternalTransfer_ReturnsMonday() {
            monday.set(Calendar.HOUR_OF_DAY, 12);
            monday.set(Calendar.MINUTE, 59);
            String nextPossibleTransferDate = SBABDateUtils.nextPossibleTransferDate(monday.getTime(), true);
            Assert.assertEquals(nextPossibleTransferDate, "2016-01-04");
        }

        @Test
        public void mondayExternalTransfer_ReturnsMonday() {
            monday.set(Calendar.HOUR_OF_DAY, 12);
            monday.set(Calendar.MINUTE, 59);
            String nextPossibleTransferDate = SBABDateUtils.nextPossibleTransferDate(monday.getTime(), false);
            Assert.assertEquals(nextPossibleTransferDate, "2016-01-04");
        }
    }

    public static class BeforeMidday {

        static Calendar monday;
        static Calendar friday;
        static Calendar sunday;

        {
            monday = Calendar.getInstance();
            monday.set(2016, Calendar.JANUARY, 4);

            friday = Calendar.getInstance();
            friday.set(2016, Calendar.JANUARY, 8);

            sunday = Calendar.getInstance();
            sunday.set(2016, Calendar.JANUARY, 10);
        }

        @Test
        public void mondayExternalTransfer_ReturnsMonday() {
            monday.set(Calendar.HOUR_OF_DAY, 9);
            String nextPossibleTransferDate = SBABDateUtils.nextPossibleTransferDate(monday.getTime(), false);
            Assert.assertEquals(nextPossibleTransferDate, "2016-01-04");
        }

        @Test
        public void mondayInternalTransfer_ReturnsMonday() {
            monday.set(Calendar.HOUR_OF_DAY, 9);
            String nextPossibleTransferDate = SBABDateUtils.nextPossibleTransferDate(monday.getTime(), true);
            Assert.assertEquals(nextPossibleTransferDate, "2016-01-04");
        }
        
        @Test
        public void fridayExternalTransfer_ReturnsFriday() {
            friday.set(Calendar.HOUR_OF_DAY, 9);
            String nextPossibleTransferDate = SBABDateUtils.nextPossibleTransferDate(friday.getTime(), false);
            Assert.assertEquals(nextPossibleTransferDate, "2016-01-08");
        }

        @Test
        public void fridayInternalTransfer_ReturnsFriday() {
            friday.set(Calendar.HOUR_OF_DAY, 9);
            String nextPossibleTransferDate = SBABDateUtils.nextPossibleTransferDate(friday.getTime(), true);
            Assert.assertEquals(nextPossibleTransferDate, "2016-01-08");
        }

        @Test
        public void sundayExternalTransfer_ReturnsMonday() {
            sunday.set(Calendar.HOUR_OF_DAY, 9);
            String nextPossibleTransferDate = SBABDateUtils.nextPossibleTransferDate(sunday.getTime(), false);
            Assert.assertEquals(nextPossibleTransferDate, "2016-01-11");
        }

        @Test
        public void sundayInternalTransfer_ReturnsSunday() {
            sunday.set(Calendar.HOUR_OF_DAY, 9);
            String nextPossibleTransferDate = SBABDateUtils.nextPossibleTransferDate(sunday.getTime(), true);
            Assert.assertEquals(nextPossibleTransferDate, "2016-01-10");
        }
    }

    public static class ExactlyMidday {

        static Calendar monday;

        static {
            monday = Calendar.getInstance();
            monday.set(2016, Calendar.JANUARY, 4);
        }

        @Test
        public void mondayExternalTransfer_ReturnsTuesday() {
            monday.set(Calendar.HOUR_OF_DAY, 13);
            monday.set(Calendar.MINUTE, 0);
            String nextPossibleTransferDate = SBABDateUtils.nextPossibleTransferDate(monday.getTime(), false);
            Assert.assertEquals(nextPossibleTransferDate, "2016-01-05");
        }

        @Test
        public void mondayInternalTransfer_ReturnsMonday() {
            monday.set(Calendar.HOUR_OF_DAY, 13);
            monday.set(Calendar.MINUTE, 0);
            String nextPossibleTransferDate = SBABDateUtils.nextPossibleTransferDate(monday.getTime(), true);
            Assert.assertEquals(nextPossibleTransferDate, "2016-01-04");
        }
    }

    public static class AfterMidday {

        static Calendar monday;
        static Calendar friday;
        static Calendar sunday;

        static {
            monday = Calendar.getInstance();
            monday.set(2016, Calendar.JANUARY, 4);

            friday = Calendar.getInstance();
            friday.set(2016, Calendar.JANUARY, 8);

            sunday = Calendar.getInstance();
            sunday.set(2016, Calendar.JANUARY, 10);
        }

        @Test
        public void mondayExternalTransfer_ReturnsTuesday() {
            monday.set(Calendar.HOUR_OF_DAY, 17);
            String nextPossibleTransferDate = SBABDateUtils.nextPossibleTransferDate(monday.getTime(), false);
            Assert.assertEquals(nextPossibleTransferDate, "2016-01-05");
        }

        @Test
        public void mondayInternalTransfer_ReturnsMonday() {
            monday.set(Calendar.HOUR_OF_DAY, 17);
            String nextPossibleTransferDate = SBABDateUtils.nextPossibleTransferDate(monday.getTime(), true);
            Assert.assertEquals(nextPossibleTransferDate, "2016-01-04");
        }

        @Test
        public void fridayExternalTransfer_ReturnsMonday() {
            friday.set(Calendar.HOUR_OF_DAY, 17);
            String nextPossibleTransferDate = SBABDateUtils.nextPossibleTransferDate(friday.getTime(), false);
            Assert.assertEquals(nextPossibleTransferDate, "2016-01-11");
        }

        @Test
        public void fridayInternalTransfer_ReturnsFriday() {
            friday.set(Calendar.HOUR_OF_DAY, 17);
            String nextPossibleTransferDate = SBABDateUtils.nextPossibleTransferDate(friday.getTime(), true);
            Assert.assertEquals(nextPossibleTransferDate, "2016-01-08");
        }

        @Test
        public void sundayExternalTransfer_ReturnsMonday() {
            sunday.set(Calendar.HOUR_OF_DAY, 17);
            String nextPossibleTransferDate = SBABDateUtils.nextPossibleTransferDate(sunday.getTime(), false);
            Assert.assertEquals(nextPossibleTransferDate, "2016-01-11");
        }

        @Test
        public void sundayInternalTransfer_ReturnsSunday() {
            sunday.set(Calendar.HOUR_OF_DAY, 17);
            String nextPossibleTransferDate = SBABDateUtils.nextPossibleTransferDate(sunday.getTime(), true);
            Assert.assertEquals(nextPossibleTransferDate, "2016-01-10");
        }
    }
}
